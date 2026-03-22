package io.cmartinezs.keygo.supabase.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entidad JPA: Clave de firma RSA para emisión de tokens JWT.
 *
 * <p>Mapea la tabla {@code signing_keys} de la base de datos.
 */
@Entity
@Table(name = "signing_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigningKeyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "kid", nullable = false, unique = true, length = 100)
  private String kid;

  @Column(name = "algorithm", nullable = false, length = 20)
  private String algorithm;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "public_material", nullable = false, columnDefinition = "TEXT")
  private String publicMaterial;

  @Column(name = "private_material", columnDefinition = "TEXT")
  private String privateMaterial;

  @Column(name = "activated_at", nullable = false)
  private Instant activatedAt;

  @Column(name = "retired_at")
  private Instant retiredAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

