package io.cmartinezs.keygo.supabase.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Permission entity for Supabase
 * Entidad de permiso para Supabase
 *
 * @author cmartinezs
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permissions_resource", columnList = "resource"),
    @Index(name = "idx_permissions_action", columnList = "action")
})
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String resource;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Permission actions / Acciones de permisos
     */
    public enum Action {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        EXECUTE
    }
}

