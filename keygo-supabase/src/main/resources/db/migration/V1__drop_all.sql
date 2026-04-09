-- ============================================================================
-- V1__drop_all.sql
-- Clean-room baseline reset for local and disposable environments.
-- Excludes flyway_schema_history so Flyway can keep tracking the remake.
-- ============================================================================

DO $$
DECLARE
    v_record RECORD;
BEGIN
    FOR v_record IN
        SELECT schemaname, matviewname AS object_name
        FROM pg_matviews
        WHERE schemaname = current_schema()
    LOOP
        EXECUTE format(
            'DROP MATERIALIZED VIEW IF EXISTS %I.%I CASCADE',
            v_record.schemaname,
            v_record.object_name
        );
    END LOOP;

    FOR v_record IN
        SELECT table_schema, table_name
        FROM information_schema.views
        WHERE table_schema = current_schema()
    LOOP
        EXECUTE format(
            'DROP VIEW IF EXISTS %I.%I CASCADE',
            v_record.table_schema,
            v_record.table_name
        );
    END LOOP;

    FOR v_record IN
        SELECT schemaname, tablename
        FROM pg_tables
        WHERE schemaname = current_schema()
          AND tablename <> 'flyway_schema_history'
    LOOP
        EXECUTE format(
            'DROP TABLE IF EXISTS %I.%I CASCADE',
            v_record.schemaname,
            v_record.tablename
        );
    END LOOP;
END $$;

DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
DROP FUNCTION IF EXISTS prevent_append_only_mutation() CASCADE;
