-- ═══════════════════════════════════════════════════════════════════════════════
-- Queries para trackear el proceso de contratación (onboarding)
-- KeyGo Server — Billing Flow Tracking
-- ═══════════════════════════════════════════════════════════════════════════════
--
-- Estados del flujo (V12 billing_contracts):
-- 1. PENDING_EMAIL_VERIFICATION → contrato creado, esperando verificación email
-- 2. PENDING_PAYMENT            → email verificado, esperando confirmación pago
-- 3. READY_TO_ACTIVATE          → pago aprobado, esperando activación final
-- 4. ACTIVE                     → contrato activo, suscripción iniciada
-- 5. SUPERSEDED                 → reemplazado por un nuevo contrato (upgrade/downgrade)
-- 6. FINALIZED                  → completado (para planes ONE_TIME)
-- 7. CANCELLED / EXPIRED / FAILED → flujo abortado
--
-- IMPORTANTE: Los contratos tienen su propio campo `verification_code` (6 dígitos, 30min TTL).
-- La tabla `email_verifications` es INDEPENDIENTE y se usa para registro de TenantUsers.
--
-- ═══════════════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────────────
-- 1️⃣ VISTA CONSOLIDADA — Estado completo de un contrato
-- ───────────────────────────────────────────────────────────────────────────────
-- 📌 Reemplazar el UUID con el ID del contrato que quieres trackear
SELECT
    -- Datos del contrato
    c.id AS contract_id,
    c.status AS contract_status,
    c.created_at AS contract_created,
    c.updated_at AS contract_updated,
    c.contractor_email,
    c.contractor_first_name,
    c.contractor_last_name,
    c.company_name,

    -- Plan contratado
    p.name AS plan_name,
    pv.version AS plan_version,
    c.billing_period AS selected_billing_period,
    pbo.base_price AS plan_base_price,

    -- Contractor (si existe)
    ctr.id AS contractor_id,
    ctr.status AS contractor_status,
    tu_ctr.email AS contractor_user_email,
    tu_ctr.first_name || ' ' || tu_ctr.last_name AS contractor_user_name,

    -- Verificación de email (del contrato)
    c.verification_code,
    c.verification_code_expires_at,
    c.email_verified_at,
    CASE
        WHEN c.email_verified_at IS NOT NULL THEN '✅ Verificado'
        WHEN c.verification_code_expires_at < NOW() THEN '⏰ Expirado'
        WHEN c.verification_code_expires_at >= NOW() THEN '⏳ Pendiente'
        ELSE '❌ Sin código'
    END AS verification_status,

    -- Verificación de pago
    c.payment_verified_at,

    -- Suscripción (si existe)
    sub.id AS subscription_id,
    sub.status AS subscription_status,
    sub.current_period_start AS subscription_start,
    sub.current_period_end AS subscription_end,
    sub.next_billing_at,

    -- Tenant creado (si existe)
    t.slug AS tenant_slug,
    t.status AS tenant_status,
    t.name AS tenant_name,

    -- Indicadores de progreso (estados reales de V12)
    CASE WHEN c.status = 'PENDING_EMAIL_VERIFICATION' THEN '1️⃣  Esperando verificación email' END AS step_1,
    CASE WHEN c.status IN ('PENDING_PAYMENT', 'READY_TO_ACTIVATE', 'ACTIVE') THEN '2️⃣  Email verificado ✅' END AS step_2,
    CASE WHEN c.status IN ('READY_TO_ACTIVATE', 'ACTIVE') THEN '3️⃣  Pago aprobado ✅' END AS step_3,
    CASE WHEN c.status = 'ACTIVE' THEN '4️⃣  Contrato activo ✅' END AS step_4

FROM app_contracts c
LEFT JOIN app_plan_versions pv ON c.selected_plan_version_id = pv.id
LEFT JOIN app_plans p ON pv.app_plan_id = p.id
LEFT JOIN app_plan_billing_options pbo ON pv.id = pbo.app_plan_version_id AND pbo.billing_period = c.billing_period
LEFT JOIN contractors ctr ON c.contractor_id = ctr.id
LEFT JOIN tenant_users tu_ctr ON ctr.tenant_user_id = tu_ctr.id
LEFT JOIN app_subscriptions sub ON sub.contract_id = c.id
LEFT JOIN tenants t ON ctr.id = t.contractor_id

WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'  -- 🔧 Reemplazar con tu contract_id
;

-- ───────────────────────────────────────────────────────────────────────────────
-- 2️⃣ LISTAR CONTRATOS ACTIVOS — Todos los onboardings en progreso
-- ───────────────────────────────────────────────────────────────────────────────
SELECT
    c.id AS contract_id,
    c.status,
    c.contractor_email,
    c.contractor_first_name || ' ' || c.contractor_last_name AS contractor_name,
    p.name AS plan,
    c.billing_period,
    c.created_at,
    EXTRACT(EPOCH FROM (NOW() - c.created_at)) / 3600 AS hours_since_creation,
    CASE
        WHEN c.status = 'ACTIVE' THEN '✅'
        WHEN c.status LIKE 'PENDING_%' OR c.status = 'READY_TO_ACTIVATE' THEN '⏳'
        WHEN c.status IN ('CANCELLED', 'EXPIRED', 'FAILED') THEN '❌'
        ELSE '📝'
    END AS icon
FROM app_contracts c
LEFT JOIN app_plan_versions pv ON c.selected_plan_version_id = pv.id
LEFT JOIN app_plans p ON pv.app_plan_id = p.id
WHERE c.status IN ('PENDING_EMAIL_VERIFICATION', 'PENDING_PAYMENT', 'READY_TO_ACTIVATE', 'ACTIVE')
ORDER BY c.created_at DESC
LIMIT 50;

-- ───────────────────────────────────────────────────────────────────────────────
-- 3️⃣ VERIFICACIONES DE EMAIL (REGISTRO DE USUARIOS) — Códigos de email_verifications
-- ───────────────────────────────────────────────────────────────────────────────
-- ⚠️ NOTA: Esta tabla es para REGISTRO DE USUARIOS (TenantUser), NO para contratos.
-- Los contratos tienen su propio campo verification_code en app_contracts.
SELECT
    ev.id AS verification_id,
    tu.email,
    tu.username,
    tu.first_name || ' ' || tu.last_name AS user_name,
    ev.code,
    ev.created_at AS code_created,
    ev.expires_at AS code_expires,
    ev.used_at AS code_used,
    EXTRACT(EPOCH FROM (ev.expires_at - NOW())) / 60 AS minutes_until_expiry,
    CASE
        WHEN ev.used_at IS NOT NULL THEN '✅ Usado'
        WHEN ev.expires_at < NOW() THEN '⏰ Expirado'
        ELSE '⏳ Válido'
    END AS status
FROM email_verifications ev
JOIN tenant_users tu ON ev.tenant_user_id = tu.id
WHERE ev.used_at IS NULL  -- Solo códigos no utilizados
  AND ev.expires_at >= NOW() - INTERVAL '24 hours'  -- Últimas 24h
ORDER BY ev.created_at DESC
LIMIT 20;

-- ───────────────────────────────────────────────────────────────────────────────
-- 4️⃣ CONTRATOS BLOQUEADOS — Detectar onboardings estancados
-- ───────────────────────────────────────────────────────────────────────────────
SELECT
    c.id AS contract_id,
    c.status,
    c.contractor_email,
    c.contractor_first_name || ' ' || c.contractor_last_name AS contractor_name,
    c.created_at,
    EXTRACT(EPOCH FROM (NOW() - c.created_at)) / 3600 AS hours_stuck,
    CASE
        WHEN c.status = 'PENDING_EMAIL_VERIFICATION' AND (NOW() - c.created_at) > INTERVAL '2 hours'
            THEN '⚠️ Email no verificado (>2h)'
        WHEN c.status = 'PENDING_PAYMENT' AND (NOW() - c.created_at) > INTERVAL '24 hours'
            THEN '⚠️ Pago pendiente (>24h)'
        WHEN c.status = 'READY_TO_ACTIVATE' AND (NOW() - c.created_at) > INTERVAL '1 hour'
            THEN '⚠️ Activación pendiente (>1h)'
        ELSE 'OK'
    END AS alert
FROM app_contracts c
WHERE c.status IN ('PENDING_EMAIL_VERIFICATION', 'PENDING_PAYMENT', 'READY_TO_ACTIVATE')
  AND (
      (c.status = 'PENDING_EMAIL_VERIFICATION' AND (NOW() - c.created_at) > INTERVAL '2 hours') OR
      (c.status = 'PENDING_PAYMENT' AND (NOW() - c.created_at) > INTERVAL '24 hours') OR
      (c.status = 'READY_TO_ACTIVATE' AND (NOW() - c.created_at) > INTERVAL '1 hour')
  )
ORDER BY c.created_at ASC;

-- ───────────────────────────────────────────────────────────────────────────────
-- 5️⃣ TIMELINE DE EVENTOS — Reconstruir historial de un contrato
-- ───────────────────────────────────────────────────────────────────────────────
WITH contract_events AS (
    SELECT
        c.id AS contract_id,
        c.created_at AS timestamp,
        'CONTRACT_CREATED' AS event,
        c.status AS status,
        'Email: ' || c.contractor_email AS detail
    FROM app_contracts c
    WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'  -- 🔧 Reemplazar

    UNION ALL

    SELECT
        c.id,
        c.email_verified_at,
        'EMAIL_VERIFIED',
        c.status,
        'Verification code used'
    FROM app_contracts c
    WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'
      AND c.email_verified_at IS NOT NULL

    UNION ALL

    SELECT
        c.id,
        c.payment_verified_at,
        'PAYMENT_VERIFIED',
        c.status,
        'Payment approved'
    FROM app_contracts c
    WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'
      AND c.payment_verified_at IS NOT NULL

    UNION ALL

    SELECT
        c.id,
        sub.created_at,
        'SUBSCRIPTION_CREATED',
        sub.status,
        'Subscription ID: ' || sub.id::text
    FROM app_contracts c
    JOIN app_subscriptions sub ON sub.contract_id = c.id
    WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'

    UNION ALL

    SELECT
        c.id,
        t.created_at,
        'TENANT_CREATED',
        t.status,
        'Slug: ' || t.slug
    FROM app_contracts c
    JOIN contractors ctr ON c.contractor_id = ctr.id
    JOIN tenants t ON t.contractor_id = ctr.id
    WHERE c.id = 'cef0ea3f-b5eb-4d58-961e-72542baeb71a'
)
SELECT
    ROW_NUMBER() OVER (ORDER BY timestamp) AS step,
    timestamp,
    event,
    status,
    detail,
    EXTRACT(EPOCH FROM (LEAD(timestamp) OVER (ORDER BY timestamp) - timestamp)) / 60 AS minutes_to_next_event
FROM contract_events
WHERE timestamp IS NOT NULL
ORDER BY timestamp;

-- ───────────────────────────────────────────────────────────────────────────────
-- 6️⃣ ESTADÍSTICAS DE CONVERSIÓN — Funnel del onboarding
-- ───────────────────────────────────────────────────────────────────────────────
SELECT
    'TOTAL CONTRATOS' AS stage,
    COUNT(*) AS count,
    100.0 AS percentage,
    NULL AS avg_time_to_next_stage_minutes
FROM app_contracts

UNION ALL

SELECT
    'PENDING_EMAIL_VERIFICATION',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM app_contracts), 0), 2),
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 60)::numeric(10,2)
FROM app_contracts
WHERE status = 'PENDING_EMAIL_VERIFICATION'

UNION ALL

SELECT
    'PENDING_PAYMENT',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM app_contracts), 0), 2),
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 60)::numeric(10,2)
FROM app_contracts
WHERE status = 'PENDING_PAYMENT'

UNION ALL

SELECT
    'READY_TO_ACTIVATE',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM app_contracts), 0), 2),
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 60)::numeric(10,2)
FROM app_contracts
WHERE status = 'READY_TO_ACTIVATE'

UNION ALL

SELECT
    'ACTIVE ✅',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM app_contracts), 0), 2),
    AVG(EXTRACT(EPOCH FROM (updated_at - created_at)) / 60)::numeric(10,2)
FROM app_contracts
WHERE status = 'ACTIVE'

UNION ALL

SELECT
    'CANCELLED / EXPIRED / FAILED ❌',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / NULLIF((SELECT COUNT(*) FROM app_contracts), 0), 2),
    NULL
FROM app_contracts
WHERE status IN ('CANCELLED', 'EXPIRED', 'FAILED')

ORDER BY percentage DESC;

-- ───────────────────────────────────────────────────────────────────────────────
-- 7️⃣ BÚSQUEDA POR EMAIL — Encontrar contrato de un contractor
-- ───────────────────────────────────────────────────────────────────────────────
-- 📌 Reemplazar el email con el que buscas
SELECT
    c.id AS contract_id,
    c.status AS contract_status,
    c.contractor_email,
    c.contractor_first_name || ' ' || c.contractor_last_name AS contractor_name,
    p.name AS plan,
    pv.version AS plan_version,
    c.billing_period,
    c.created_at,
    CASE
        WHEN c.status = 'ACTIVE' THEN '✅ Activo'
        WHEN c.status LIKE 'PENDING_%' OR c.status = 'READY_TO_ACTIVATE' THEN '⏳ En proceso'
        ELSE c.status
    END AS friendly_status
FROM app_contracts c
LEFT JOIN app_plan_versions pv ON c.selected_plan_version_id = pv.id
LEFT JOIN app_plans p ON pv.app_plan_id = p.id
WHERE c.contractor_email = 'contractor@keygo.local'  -- 🔧 Reemplazar con el email
ORDER BY c.created_at DESC;

-- ───────────────────────────────────────────────────────────────────────────────
-- 8️⃣ CÓDIGOS DE VERIFICACIÓN DE CONTRATOS — Estado de verificaciones pendientes
-- ───────────────────────────────────────────────────────────────────────────────
-- ⚠️ NOTA: Esta query muestra los códigos de verificación DE CONTRATOS (app_contracts),
-- NO los de registro de usuarios (email_verifications). Son sistemas independientes.
SELECT
    c.id AS contract_id,
    c.contractor_email,
    c.contractor_first_name || ' ' || c.contractor_last_name AS contractor_name,
    c.verification_code,
    c.verification_code_expires_at AS code_expires,
    c.email_verified_at AS verified_at,
    EXTRACT(EPOCH FROM (c.verification_code_expires_at - NOW())) / 60 AS minutes_until_expiry,
    CASE
        WHEN c.email_verified_at IS NOT NULL THEN '✅ Verificado'
        WHEN c.verification_code_expires_at < NOW() THEN '⏰ Expirado'
        WHEN c.verification_code_expires_at >= NOW() THEN '⏳ Válido'
        ELSE '❌ Sin código'
    END AS status,
    c.status AS contract_status,
    p.name AS plan
FROM app_contracts c
LEFT JOIN app_plan_versions pv ON c.selected_plan_version_id = pv.id
LEFT JOIN app_plans p ON pv.app_plan_id = p.id
WHERE c.email_verified_at IS NULL  -- Solo contratos con email NO verificado
  AND c.status = 'PENDING_EMAIL_VERIFICATION'
  AND c.created_at >= NOW() - INTERVAL '24 hours'  -- Últimas 24h
ORDER BY c.created_at DESC
LIMIT 20;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 📖 GUÍA DE USO
-- ═══════════════════════════════════════════════════════════════════════════════
--
-- Query 1 (VISTA CONSOLIDADA):
--   → Usa este para ver el estado completo de un contrato específico
--   → Reemplaza el UUID en el WHERE con el ID del contrato
--   → Te muestra: verificación email, pago, tenant, suscripción, progreso
--
-- Query 2 (LISTAR CONTRATOS ACTIVOS):
--   → Ver todos los onboardings en progreso (no completados ni cancelados)
--   → Útil para dashboard de administración
--
-- Query 3 (VERIFICACIONES DE EMAIL - USUARIOS):
--   → Ver códigos de verificación de REGISTRO DE USUARIOS (email_verifications)
--   → Detectar códigos expirados que necesitan reenvío
--   → ⚠️ Esto NO es para contratos, es para TenantUsers
--
-- Query 4 (CONTRATOS BLOQUEADOS):
--   → Detectar onboardings que llevan demasiado tiempo en un estado
--   → Alertas automáticas para intervención manual
--
-- Query 5 (TIMELINE DE EVENTOS):
--   → Reconstruir el historial completo de un contrato
--   → Ver cuánto tiempo tomó cada paso del proceso
--
-- Query 6 (ESTADÍSTICAS DE CONVERSIÓN):
--   → Funnel completo del onboarding
--   → Tasas de conversión y tiempos promedio
--
-- Query 7 (BÚSQUEDA POR EMAIL):
--   → Encontrar contratos de un usuario por email
--   → Útil para soporte al cliente
--
-- Query 8 (CÓDIGOS DE VERIFICACIÓN - CONTRATOS):
--   → Ver códigos de verificación de CONTRATOS (app_contracts.verification_code)
--   → Detectar contratos con verificación pendiente o expirada
--   → ⚠️ Esto es INDEPENDIENTE de email_verifications (Query 3)
--
-- ═══════════════════════════════════════════════════════════════════════════════

