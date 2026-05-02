package io.cmartinezs.keygo.supabase.clientapp.entity;

import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity for a client application allowed OAuth2 grant type.
 * <p>Entidad JPA para un grant type OAuth2 permitido de una aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "client_allowed_grants",
    indexes = @Index(name = "idx_allowed_grants_app", columnList = "client_app_id"))
public class ClientAllowedGrantEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @Enumerated(EnumType.STRING)
  @Column(name = "grant_type", nullable = false, length = 50)
  private AllowedGrant grantType;
}

