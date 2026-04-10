# Security & Compliance Documentation

Security guidelines, threat modeling, OWASP defense, and compliance requirements.

---

## 🔒 Quick Start

**Need to understand security requirements?**
- [`SECURITY_GUIDELINES.md`](SECURITY_GUIDELINES.md) — Complete security reference

**Implementing a feature?**
1. Check [`SECURITY_GUIDELINES.md`](SECURITY_GUIDELINES.md) for OWASP mitigations
2. Review secrets management approach
3. Ensure encryption is used where needed

**Deploying to production?**
- See [`../operations/DEPLOYMENT_PIPELINE.md`](../operations/DEPLOYMENT_PIPELINE.md) — Security scanning (SAST, SBOM, container)

---

## 📖 Documentation Index

| Document | Purpose | For Whom |
|---|---|---|
| SECURITY_GUIDELINES.md | OWASP Top 10 defenses, threat modeling, secrets, compliance | All architects, security-conscious devs |

---

## 🎯 Key Topics

### OWASP Top 10 Defense
- **Broken Authentication** — OAuth2 PKCE, JWT expiry, refresh rotation, rate limiting, account lockout
- **Broken Access Control** — RBAC, tenant isolation, row-level security (@PreAuthorize)
- **Injection** — Parameterized queries (JPA), input validation, escaping
- **Insecure Design** — Threat modeling (STRIDE), security architecture reviews
- **Cryptographic Failures** — AES-256 at rest, TLS 1.2+ in transit, bcrypt/argon2 passwords
- **Vulnerable Components** — Snyk scanning, monthly patches, CVE monitoring
- **Identification Failures** — Session timeout (15 min idle, 8h max), token binding, logout revokes
- **Integrity Failures** — Signed artifacts (Cosign), checksum verification
- **Logging Failures** — Audit logs, alerting, immutable logs (SIEM)
- **SSRF** — Whitelist URLs, disable internal access, validate redirects

### Secrets Management
- Environment variables for development
- HashiCorp Vault for production
- GitHub Secrets for CI/CD
- Secret rotation policy (quarterly minimum)

### Data Protection
- GDPR: Minimal collection, right to deletion, right to access
- Soft-delete pattern for compliance
- Data export functionality

### API Security
- OAuth2 with PKCE flow
- CORS: specific origins only
- Input validation (whitelist, length, regex)
- Rate limiting (auth endpoints, API quotas)

### Security Headers
- X-Frame-Options: DENY
- X-Content-Type-Options: nosniff
- Strict-Transport-Security (HSTS)
- Content-Security-Policy (CSP)

---

## 🔗 Related Documentation

- **API Security:** [`../design/OAUTH2_MULTIDOMAIN_CONTRACT.md`](../design/OAUTH2_MULTIDOMAIN_CONTRACT.md)
- **Authorization Model:** [`../design/AUTHORIZATION_PATTERNS.md`](../design/AUTHORIZATION_PATTERNS.md)
- **Deployment Security:** [`../operations/DEPLOYMENT_PIPELINE.md`](../operations/DEPLOYMENT_PIPELINE.md)
- **All Architecture:** [`../design/`](../design/)

---

## ✅ Security Checklist

Before deploying to production:

- [ ] Authentication: OAuth2 PKCE implemented, no hardcoded credentials
- [ ] Authorization: RBAC configured, tenant isolation verified
- [ ] Data Encryption: Secrets in Vault, passwords hashed (bcrypt/argon2)
- [ ] HTTPS: TLS 1.2+, valid certificate, HSTS header
- [ ] Input Validation: All inputs validated, whitelist approach
- [ ] Logging: Audit logs for sensitive operations, no PII in logs
- [ ] Secrets: No secrets in code, env vars or Vault only
- [ ] Dependencies: Snyk scan passed, no high/critical CVEs
- [ ] CORS: Configured for specific origins, credentials: false for public
- [ ] Rate Limiting: Implemented on auth, API endpoints
- [ ] CSRF: CSRF tokens on forms, SameSite=Strict on cookies
- [ ] Headers: Security headers set (CSP, X-Frame-Options, etc.)
- [ ] Code Review: Security-focused review completed

---

**Last updated:** 2026-04-10  
**Status:** Complete (Sprint 4)  
**Coverage:** 100% of OWASP Top 10, secrets, compliance
