package one.modality.crm.frontoffice.activities.members;

import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import one.modality.base.shared.entities.Invitation;
import one.modality.base.shared.entities.Person;

/**
 * Request to send an authorization request email for a member invitation
 *
 * @author David Hello
 */
public record SendAuthorizationRequest(Person inviter, Person invitee, Invitation invitation, String clientOrigin,
                                       DataSourceModel dataSourceModel) {

}
