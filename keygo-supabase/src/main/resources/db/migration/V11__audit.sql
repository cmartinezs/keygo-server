-- ============================================================================
-- V11__audit.sql
-- Append-only audit model for UI, backend, billing and security observability.
-- ============================================================================

CREATE TABLE audit_events (
    id                        UUID           NOT NULL DEFAULT gen_random_uuid(),
    occurred_at               TIMESTAMPTZ    NOT NULL DEFAULT now(),
    actor_type                VARCHAR(30)    NOT NULL,
    actor_platform_user_id    UUID,
    actor_tenant_user_id      UUID,
    contractor_id             UUID,
    tenant_id                 UUID,
    client_app_id             UUID,
    platform_session_id       UUID,
    oauth_session_id          UUID,
    source_layer              VARCHAR(30)    NOT NULL,
    source_channel            VARCHAR(30)    NOT NULL,
    source_app                VARCHAR(100),
    source_module             VARCHAR(100),
    source_feature            VARCHAR(100),
    source_screen             VARCHAR(150),
    source_route              VARCHAR(500),
    request_id                VARCHAR(100),
    correlation_id            VARCHAR(100),
    trace_id                  VARCHAR(100),
    span_id                   VARCHAR(100),
    http_method               VARCHAR(10),
    http_path                 VARCHAR(1000),
    http_status_code          INTEGER,
    event_category            VARCHAR(50)    NOT NULL,
    event_type                VARCHAR(100)   NOT NULL,
    event_action              VARCHAR(100)   NOT NULL,
    event_outcome             VARCHAR(20)    NOT NULL,
    severity                  VARCHAR(20)    NOT NULL DEFAULT 'INFO',
    target_type               VARCHAR(100),
    target_id                 UUID,
    target_display            VARCHAR(255),
    summary                   TEXT           NOT NULL,
    metadata                  JSONB          NOT NULL DEFAULT '{}'::jsonb,
    duration_ms               BIGINT,
    records_affected          INTEGER,
    risk_score                NUMERIC(5,2),
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_events PRIMARY KEY (id),
    CONSTRAINT fk_audit_events_actor_platform_user
        FOREIGN KEY (actor_platform_user_id) REFERENCES platform_users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_actor_tenant_user
        FOREIGN KEY (actor_tenant_user_id) REFERENCES tenant_users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_client_app
        FOREIGN KEY (client_app_id) REFERENCES client_apps(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_platform_session
        FOREIGN KEY (platform_session_id) REFERENCES platform_sessions(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_oauth_session
        FOREIGN KEY (oauth_session_id) REFERENCES oauth_sessions(id) ON DELETE SET NULL,
    CONSTRAINT chk_audit_events_actor_type CHECK (
        actor_type IN ('USER', 'SYSTEM', 'SERVICE', 'ANONYMOUS')
    ),
    CONSTRAINT chk_audit_events_source_layer CHECK (
        source_layer IN ('UI', 'BACKEND', 'SYSTEM', 'JOB', 'SECURITY', 'BILLING')
    ),
    CONSTRAINT chk_audit_events_source_channel CHECK (
        source_channel IN ('WEB', 'API', 'WORKER', 'INTERNAL', 'SCHEDULED')
    ),
    CONSTRAINT chk_audit_events_outcome CHECK (
        event_outcome IN ('SUCCESS', 'FAILURE', 'DENIED', 'ERROR', 'PARTIAL')
    ),
    CONSTRAINT chk_audit_events_severity CHECK (
        severity IN ('DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL')
    )
);

CREATE INDEX idx_audit_events_occurred_at ON audit_events(occurred_at DESC);
CREATE INDEX idx_audit_events_actor_platform_user_time ON audit_events(actor_platform_user_id, occurred_at DESC);
CREATE INDEX idx_audit_events_actor_tenant_user_time ON audit_events(actor_tenant_user_id, occurred_at DESC);
CREATE INDEX idx_audit_events_tenant_time ON audit_events(tenant_id, occurred_at DESC);
CREATE INDEX idx_audit_events_contractor_time ON audit_events(contractor_id, occurred_at DESC);
CREATE INDEX idx_audit_events_client_app_time ON audit_events(client_app_id, occurred_at DESC);
CREATE INDEX idx_audit_events_category_time ON audit_events(event_category, occurred_at DESC);
CREATE INDEX idx_audit_events_type_time ON audit_events(event_type, occurred_at DESC);
CREATE INDEX idx_audit_events_outcome_time ON audit_events(event_outcome, occurred_at DESC);
CREATE INDEX idx_audit_events_platform_session ON audit_events(platform_session_id);
CREATE INDEX idx_audit_events_oauth_session ON audit_events(oauth_session_id);
CREATE INDEX idx_audit_events_request_id ON audit_events(request_id);
CREATE INDEX idx_audit_events_correlation_id ON audit_events(correlation_id);
CREATE INDEX idx_audit_events_trace_id ON audit_events(trace_id);

CREATE TABLE audit_event_payloads (
    audit_event_id     UUID         NOT NULL,
    request_payload    JSONB,
    response_payload   JSONB,
    before_state       JSONB,
    after_state        JSONB,
    diff_payload       JSONB,
    extra_context      JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_event_payloads PRIMARY KEY (audit_event_id),
    CONSTRAINT fk_audit_event_payloads_event
        FOREIGN KEY (audit_event_id) REFERENCES audit_events(id) ON DELETE CASCADE
);

CREATE TABLE audit_event_tags (
    audit_event_id  UUID          NOT NULL,
    tag             VARCHAR(100)  NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_event_tags PRIMARY KEY (audit_event_id, tag),
    CONSTRAINT fk_audit_event_tags_event
        FOREIGN KEY (audit_event_id) REFERENCES audit_events(id) ON DELETE CASCADE
);

CREATE TABLE audit_entity_links (
    id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
    audit_event_id      UUID          NOT NULL,
    linked_entity_type  VARCHAR(100)  NOT NULL,
    linked_entity_id    UUID          NOT NULL,
    relation_type       VARCHAR(50)   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_audit_entity_links PRIMARY KEY (id),
    CONSTRAINT fk_audit_entity_links_event
        FOREIGN KEY (audit_event_id) REFERENCES audit_events(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_audit_events_append_only
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW
    EXECUTE FUNCTION prevent_append_only_mutation();

CREATE TRIGGER trg_audit_event_payloads_append_only
    BEFORE UPDATE OR DELETE ON audit_event_payloads
    FOR EACH ROW
    EXECUTE FUNCTION prevent_append_only_mutation();

CREATE TRIGGER trg_audit_event_tags_append_only
    BEFORE UPDATE OR DELETE ON audit_event_tags
    FOR EACH ROW
    EXECUTE FUNCTION prevent_append_only_mutation();

CREATE TRIGGER trg_audit_entity_links_append_only
    BEFORE UPDATE OR DELETE ON audit_entity_links
    FOR EACH ROW
    EXECUTE FUNCTION prevent_append_only_mutation();

COMMENT ON TABLE audit_events IS 'Primary append-only audit ledger for platform, tenant, app, billing and security actions.';
COMMENT ON TABLE audit_event_payloads IS 'Heavy payloads stored separately to keep audit aggregates fast.';
COMMENT ON TABLE audit_event_tags IS 'Searchable tags for audit events.';
COMMENT ON TABLE audit_entity_links IS 'Links from one audit event to multiple impacted entities.';
