package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Role entity
 * Repositorio para la entidad Role
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    /**
     * Find role by name
     * Buscar rol por nombre
     *
     * @param name Role name / Nombre del rol
     * @return Optional role / Rol opcional
     */
    Optional<RoleEntity> findByName(String name);

    /**
     * Check if role exists by name
     * Verificar si el rol existe por nombre
     *
     * @param name Role name / Nombre del rol
     * @return True if exists / Verdadero si existe
     */
    boolean existsByName(String name);
}

