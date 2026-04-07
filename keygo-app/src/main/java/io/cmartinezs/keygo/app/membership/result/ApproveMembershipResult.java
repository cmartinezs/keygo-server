package io.cmartinezs.keygo.app.membership.result;

import io.cmartinezs.keygo.domain.membership.model.Membership;

/**
 * Resultado de {@link io.cmartinezs.keygo.app.membership.usecase.ApproveMembershipUseCase}.
 *
 * @param membership    la membresía aprobada
 * @param maskedEmail   email ofuscado del usuario notificado (null si la notificación falló)
 * @author cmartinezs
 * @version 1.0
 */
public record ApproveMembershipResult(Membership membership, String maskedEmail) {}
