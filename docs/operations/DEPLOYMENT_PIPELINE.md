# Deployment Pipeline — CI/CD y Automatización

**Propósito:** Documentar proceso de CI/CD, automated testing, security scanning, artifact management y deployment strategies.

---

## Architecture: GitHub Actions Pipeline

```
Push to develop
    ↓
GitHub Actions workflow
    ├─ Build & Test
    │  ├─ Unit tests
    │  ├─ Integration tests
    │  └─ Code coverage check
    ├─ Security Scanning
    │  ├─ SAST (SonarQube)
    │  ├─ Dependency check (Snyk)
    │  └─ Container scan (Trivy)
    ├─ Build Artifact
    │  ├─ Docker image build
    │  ├─ Push to registry
    │  └─ Sign image (Cosign)
    └─ Deploy
       ├─ Dev → Auto-deploy
       ├─ Staging → Manual approval
       └─ Prod → Manual approval + runbook
```

---

## CI Pipeline: GitHub Actions Workflow

### File: `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches:
      - develop
      - main
  pull_request:
    branches:
      - develop

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}/keygo-server

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: keygo_test
          POSTGRES_USER: keygo
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for SonarQube
      
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
          cache: maven
      
      - name: Run unit tests
        run: ./mvnw clean test -DskipITs
      
      - name: Run integration tests
        run: ./mvnw verify -DskipTests=false
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
          fail_ci_if_error: false
  
  security:
    runs-on: ubuntu-latest
    needs: test
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
          cache: maven
      
      - name: SonarQube scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
          SONAR_HOST_URL: https://sonarqube.internal.keygo.io
        run: |
          ./mvnw clean verify sonar:sonar \
            -Dsonar.projectKey=keygo-server \
            -Dsonar.host.url=$SONAR_HOST_URL \
            -Dsonar.login=$SONAR_TOKEN
      
      - name: Snyk dependency check
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        run: |
          npm install -g snyk
          snyk auth $SNYK_TOKEN
          snyk test --severity-threshold=high
      
      - name: Check quality gate
        run: |
          QUALITY=$(curl -s -u "${{ secrets.SONAR_TOKEN }}" \
            "https://sonarqube.internal.keygo.io/api/qualitygates/project_status?projectKey=keygo-server" \
            | jq -r '.projectStatus.status')
          
          if [ "$QUALITY" != "OK" ]; then
            echo "❌ Quality gate failed"
            exit 1
          fi
  
  build:
    runs-on: ubuntu-latest
    needs: [test, security]
    if: github.event_name == 'push' && (github.ref == 'refs/heads/develop' || github.ref == 'refs/heads/main')
    
    permissions:
      contents: read
      packages: write
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
          cache: maven
      
      - name: Build JAR
        run: ./mvnw clean package -DskipTests
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Log in to Container Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v4
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}
            type=sha,prefix={{branch}}-
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:buildcache
          cache-to: type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:buildcache,mode=max
      
      - name: Scan image with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          format: sarif
          output: trivy-results.sarif
          severity: 'CRITICAL,HIGH'
      
      - name: Upload Trivy results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: trivy-results.sarif
      
      - name: Sign image with Cosign
        env:
          COSIGN_KEY: ${{ secrets.COSIGN_KEY }}
          COSIGN_PASSWORD: ${{ secrets.COSIGN_PASSWORD }}
        run: |
          cosign sign --key env://COSIGN_KEY \
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}@${{ steps.build.outputs.digest }}
```

---

## CD Pipeline: Deployment Strategy

### Three Environments

#### 1. Development (Auto-Deploy)

```yaml
# .github/workflows/deploy-dev.yml

name: Deploy to Dev

on:
  workflow_run:
    workflows: ["CI"]
    types: [completed]
    branches: [develop]

jobs:
  deploy:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Update Helm values
        run: |
          # Update image tag in values-dev.yaml
          sed -i "s/tag:.*/tag: ${{ github.sha }}/g" \
            helm/values-dev.yaml
      
      - name: Deploy with Helm
        run: |
          helm upgrade keygo-server ./helm \
            -f helm/values-dev.yaml \
            --namespace development \
            --wait \
            --timeout 5m \
            --atomic
      
      - name: Verify deployment
        run: |
          kubectl rollout status deployment/keygo-server \
            -n development --timeout=5m
          
          # Health check
          kubectl run health-check --image=curlimages/curl \
            -n development --rm -it --restart=Never -- \
            curl -f http://keygo-server:8080/actuator/health
      
      - name: Slack notification
        if: always()
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_DEV }}
          payload: |
            {
              "text": "Deployed to dev: ${{ job.status }}",
              "commit": "${{ github.sha }}",
              "branch": "${{ github.ref }}"
            }
```

#### 2. Staging (Manual Approval)

```yaml
# .github/workflows/deploy-staging.yml

name: Deploy to Staging

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to deploy (git tag or sha)'
        required: true

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment:
      name: staging
      url: https://api-staging.keygo.io
    
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.version }}
      
      - name: Pre-deployment checklist
        run: |
          echo "📋 Pre-deployment checks:"
          echo "✓ Database backup scheduled"
          echo "✓ Rollback plan documented"
          echo "✓ Monitoring alerts configured"
          echo "✓ Team notified in #deployments"
      
      - name: Deploy with Helm
        run: |
          sed -i "s/tag:.*/tag: ${{ github.event.inputs.version }}/g" \
            helm/values-staging.yaml
          
          helm upgrade keygo-server ./helm \
            -f helm/values-staging.yaml \
            --namespace staging \
            --wait \
            --timeout 10m \
            --atomic
      
      - name: Smoke tests
        run: |
          # Test critical endpoints
          curl -f https://api-staging.keygo.io/api/v1/health
          curl -f https://api-staging.keygo.io/actuator/health
          
          # Test database connectivity
          curl -f https://api-staging.keygo.io/actuator/health/readiness
      
      - name: Slack notification
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_DEPLOYMENTS }}
          payload: |
            {
              "text": "✅ Deployed to staging",
              "version": "${{ github.event.inputs.version }}",
              "deployed_by": "${{ github.actor }}"
            }
```

#### 3. Production (Manual Approval + Runbook)

```yaml
# .github/workflows/deploy-prod.yml

name: Deploy to Production

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to deploy (git tag)'
        required: true
      skip_tests:
        description: 'Skip smoke tests (emergency only)'
        required: false
        default: 'false'

jobs:
  pre-deploy-checks:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.version }}
      
      - name: Verify git tag exists
        run: |
          git fetch origin tag ${{ github.event.inputs.version }}
          if [ $? -ne 0 ]; then
            echo "❌ Tag not found: ${{ github.event.inputs.version }}"
            exit 1
          fi
      
      - name: Verify tag is signed
        run: |
          git verify-tag ${{ github.event.inputs.version }}
          if [ $? -ne 0 ]; then
            echo "❌ Tag not GPG signed"
            exit 1
          fi
      
      - name: Check deployment runbook
        run: |
          # Verify runbook is documented
          if ! grep -q "${{ github.event.inputs.version }}" \
              docs/operations/DEPLOYMENT_RUNBOOK.md; then
            echo "⚠️  Please document deployment steps in runbook"
          fi

  deploy:
    runs-on: ubuntu-latest
    needs: pre-deploy-checks
    environment:
      name: production
      url: https://api.keygo.io
    
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.version }}
      
      - name: Notify team
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_DEPLOYMENTS }}
          payload: |
            {
              "text": "🚀 Starting production deployment",
              "version": "${{ github.event.inputs.version }}",
              "deployed_by": "${{ github.actor }}",
              "runbook": "https://github.com/${{ github.repository }}/blob/main/docs/operations/PRODUCTION_RUNBOOK.md"
            }
      
      - name: Create backup
        run: |
          # Trigger database backup
          kubectl exec -n production keygo-postgres -- \
            pg_dump -U keygo keygo | gzip > /tmp/backup.sql.gz
          
          # Upload to S3
          aws s3 cp /tmp/backup.sql.gz \
            s3://keygo-backups/prod-$(date +%Y%m%d_%H%M%S).sql.gz
      
      - name: Deploy with Helm (rolling update)
        run: |
          sed -i "s/tag:.*/tag: ${{ github.event.inputs.version }}/g" \
            helm/values-prod.yaml
          
          helm upgrade keygo-server ./helm \
            -f helm/values-prod.yaml \
            --namespace production \
            --wait \
            --timeout 15m \
            --atomic \
            --history-max 5
      
      - name: Smoke tests
        if: ${{ github.event.inputs.skip_tests == 'false' }}
        run: |
          echo "Running smoke tests..."
          
          # Health check
          curl -f https://api.keygo.io/actuator/health
          
          # Critical API endpoints
          curl -f https://api.keygo.io/api/v1/platform/health
          curl -f https://api.keygo.io/oauth/authorize?client_id=test
          
          # Database connectivity
          curl -f https://api.keygo.io/actuator/health/readiness
      
      - name: Verify metrics
        run: |
          # Check error rate is normal
          ERROR_RATE=$(curl -s https://api.keygo.io/actuator/prometheus | \
            grep 'http_requests_total{status="5xx"}' | awk '{print $2}')
          
          if [ $(echo "$ERROR_RATE > 5" | bc) -eq 1 ]; then
            echo "⚠️  High error rate detected: $ERROR_RATE"
            # Could trigger automatic rollback here
          fi
      
      - name: Success notification
        if: success()
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_DEPLOYMENTS }}
          payload: |
            {
              "text": "✅ Production deployment successful",
              "version": "${{ github.event.inputs.version }}",
              "status_page": "https://api.keygo.io/actuator/health"
            }
      
      - name: Failure notification
        if: failure()
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK_DEPLOYMENTS }}
          payload: |
            {
              "text": "❌ Production deployment FAILED",
              "version": "${{ github.event.inputs.version }}",
              "runbook": "https://github.com/${{ github.repository }}/blob/main/docs/operations/PRODUCTION_RUNBOOK.md"
            }
```

---

## Artifact Management

### Docker Registry Strategy

**Images:**
```
ghcr.io/cmartinezs/keygo-server:develop-abc123      (branch + sha)
ghcr.io/cmartinezs/keygo-server:v1.0.0              (semver tag)
ghcr.io/cmartinezs/keygo-server:latest              (latest release)
ghcr.io/cmartinezs/keygo-server:buildcache          (build cache layer)
```

**Retention Policy:**
```
# .github/workflows/cleanup-registry.yml

name: Cleanup Old Images

on:
  schedule:
    - cron: '0 2 * * 0'  # Weekly

jobs:
  cleanup:
    runs-on: ubuntu-latest
    steps:
      - name: Delete images older than 30 days
        env:
          REGISTRY_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          # Keep last 10 images per branch
          # Delete untagged images > 7 days old
          # Keep all semantic version tags
```

---

## Security Scanning

### SAST: SonarQube

**Thresholds:**
```yaml
sonar.qualitygate.wait=true
sonar.qualitygate.timeout=300

# Fail if:
# - Code Smells > 50
# - Bugs > 5
# - Vulnerabilities > 0
# - Coverage < 75%
# - Duplications > 10%
```

### Dependency Scanning: Snyk

```bash
# Check for known vulnerabilities
snyk test --severity-threshold=high

# Monitor for new vulnerabilities
snyk monitor
```

**Allowed in workflow:**
```yaml
# Only from approved sources
- maven.apache.org
- repo.maven.apache.org
- spring.io/release
- spring.io/snapshot
```

### Container Scanning: Trivy

```bash
# Scan before push
trivy image --severity HIGH,CRITICAL --exit-code 1 \
  ghcr.io/cmartinezs/keygo-server:v1.0.0
```

**Exceptions allowed:**
```yaml
# Only with approval + expiry date
- CVE-2024-1234: low risk, patched in v1.0.1 (expires 2024-06-01)
```

---

## Helm Configuration

### File: `helm/values.yaml`

```yaml
replicaCount: 3

image:
  registry: ghcr.io
  repository: cmartinezs/keygo-server
  tag: develop-abc123
  pullPolicy: IfNotPresent

deployment:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: api.keygo.io
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: keygo-tls
      hosts:
        - api.keygo.io

resources:
  limits:
    cpu: 2
    memory: 3Gi
  requests:
    cpu: 1
    memory: 2Gi

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
```

### Helm Deploy Commands

```bash
# Dry run first
helm upgrade keygo-server ./helm \
  -f helm/values-prod.yaml \
  --namespace production \
  --dry-run \
  --debug

# Actual deploy
helm upgrade keygo-server ./helm \
  -f helm/values-prod.yaml \
  --namespace production \
  --wait \
  --timeout 15m \
  --atomic

# Rollback if needed
helm rollout history keygo-server -n production
helm rollout undo keygo-server -n production --revision=5
```

---

## Deployment Checklist

### Before Deployment

- [ ] All tests passing (unit, integration, contract)
- [ ] Code coverage ≥ 75%
- [ ] SonarQube quality gate passed
- [ ] Snyk security scan passed (no high/critical CVEs)
- [ ] Docker image signed with Cosign
- [ ] Database migrations tested (on staging)
- [ ] Rollback plan documented
- [ ] Monitoring alerts configured
- [ ] Team notified (#deployments channel)

### During Deployment

- [ ] Backup created
- [ ] Rolling update in progress
- [ ] No more than 1 pod unavailable
- [ ] Smoke tests passing
- [ ] Metrics normal (error rate < 1%, latency < 2s)

### After Deployment

- [ ] All pods healthy
- [ ] Logs look good (no ERRORs)
- [ ] Metrics stable
- [ ] Users not reporting issues
- [ ] Runbook updated (if needed)
- [ ] Retrospective scheduled (if issues)

---

## Rollback Procedures

### Automatic Rollback

```yaml
# If health check fails post-deploy
kubectl rollout undo deployment/keygo-server -n production
```

### Manual Rollback

```bash
# List versions
helm history keygo-server -n production

# Rollback to previous
helm rollout undo keygo-server -n production

# Rollback to specific revision
helm rollout undo keygo-server -n production --revision=3

# Verify
kubectl rollout status deployment/keygo-server -n production
```

---

## Disaster Recovery

### Database Restore

```bash
# List backups
aws s3 ls s3://keygo-backups/

# Restore
gunzip < s3://keygo-backups/prod-20260410_120000.sql.gz | \
  kubectl exec -i -n production keygo-postgres -- \
  psql -U keygo keygo

# Verify
kubectl logs -f -n production deployment/keygo-server
```

### Complete Cluster Recovery

```bash
# Redeploy entire cluster
kubectl delete namespace production
kubectl create namespace production

helm install keygo-server ./helm \
  -f helm/values-prod.yaml \
  --namespace production \
  --wait
```

---

## Anti-Patterns: Evitar

### ❌ Manual deployments

```bash
# MAL
ssh prod-server
docker pull image:tag
docker run -d image:tag
```

### ✅ Automated with approval

```bash
# BIEN
# GitHub Actions workflow with environment approval
```

---

### ❌ Deploying without tests

```yaml
# MAL
build-and-deploy:
  runs-on: ubuntu-latest
  steps:
    - docker build
    - helm upgrade  # Without tests!
```

### ✅ Test → Build → Deploy pipeline

```yaml
# BIEN
test:
  - unit tests
  - integration tests
build:
  - docker image
security:
  - SAST
  - Dependency check
deploy:
  - staging (auto)
  - prod (manual approval)
```

---

## Checklist: New Deployment

- [ ] **Environment:** dev/staging/prod correctly configured?
- [ ] **Approval:** Manual approval required for prod?
- [ ] **Tests:** Smoke tests run post-deploy?
- [ ] **Rollback:** Rollback plan documented?
- [ ] **Notifications:** Team notified in Slack?
- [ ] **Monitoring:** Metrics/logs checked?
- [ ] **Backup:** Database backup taken (prod only)?

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **GitHub Actions** | `.github/workflows/*.yml` |
| **Helm Charts** | `helm/` directory |
| **Production Runbook** | `docs/operations/PRODUCTION_RUNBOOK.md` |
| **Security Guidelines** | `docs/security/SECURITY_GUIDELINES.md` |
| **Observability** | `docs/design/OBSERVABILITY.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 4  
**Próxima:** DATABASE_SCHEMA.md
