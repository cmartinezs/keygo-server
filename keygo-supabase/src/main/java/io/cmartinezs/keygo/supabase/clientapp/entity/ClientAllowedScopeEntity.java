package io.cmartinezs.keygo.supabase.clientapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity for a client application allowed OAuth2 scope.
 * <p>Entidad JPA para un scope OAuth2 permitido de una aplicación cliente.
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
    name = "client_allowed_scopes",
    indexes = @Index(name = "idx_allowed_scopes_app", columnList = "client_app_id"))
public class ClientAllowedScopeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @Column(nullable = false, length = 100)
  private String scope;
}

