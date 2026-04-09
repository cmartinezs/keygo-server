-- =============================================================================
-- V2: Foundation — extensiones PostgreSQL y funciones utilitarias
-- =============================================================================

-- Extensión para generación de UUIDs (uuid_generate_v4 y gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Función trigger: actualiza updated_at automáticamente en cada UPDATE.
-- Se aplica a todas las tablas que tengan columna updated_at.
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

