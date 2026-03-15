package io.cmartinezs.keygo.supabase.repository;

import io.cmartinezs.keygo.supabase.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 * Repositorio para la entidad User
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find user by username
     * Buscar usuario por nombre de usuario
     *
     * @param username Username / Nombre de usuario
     * @return Optional user / Usuario opcional
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Find user by email
     * Buscar usuario por email
     *
     * @param email Email
     * @return Optional user / Usuario opcional
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Check if username exists
     * Verificar si el nombre de usuario existe
     *
     * @param username Username / Nombre de usuario
     * @return True if exists / Verdadero si existe
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * Verificar si el email existe
     *
     * @param email Email
     * @return True if exists / Verdadero si existe
     */
    boolean existsByEmail(String email);
}

