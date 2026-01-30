package one.modality.crm.backoffice.organization.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.authn.login.ui.FXLoginContext;
import dev.webfx.stack.authz.client.context.AuthorizationContext;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.session.Session;
import dev.webfx.stack.session.state.client.fx.FXSession;
import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.context.ModalityContext;
import one.modality.base.shared.entities.Organization;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXOrganizationId {

    private static final String SESSION_ORGANIZATION_ID_KEY = "fxOrganizationId";

    private final static ObjectProperty<EntityId> organizationIdProperty = FXProperties.newObjectProperty(organizationId -> {
        // Storing this new value (more precisely the primary key) in the session, and save it
        Session session = FXSession.getSession();
        Object organizationPrimaryKey = Entities.getPrimaryKey(organizationId);
        if (session != null) {
            session.put(SESSION_ORGANIZATION_ID_KEY, organizationPrimaryKey);
            session.store();
        }
        // Also resetting the FXLoginContext
        FXLoginContext.setLoginContext(new ModalityContext(organizationPrimaryKey, null, null, null));
        // Synchronizing FXOrganization to match that new organization id (FXOrganizationId => FXOrganization)
        if (!Entities.samePrimaryKey(FXOrganization.getOrganizationId(), organizationPrimaryKey)) { // Sync only if ids differ.
            // If the new organization id is null, we set the FXOrganization to null
            if (organizationPrimaryKey == null)
                FXOrganization.setOrganization(null);
            else {
                // Getting the organization store
                EntityStore organizationStore = FXOrganization.getOrganizationStore();
                // Checking if we can find the organization in memory in that store
                Organization organization = organizationStore.getEntity(organizationId);
                // If yes, there is no need to request the server, we use directly that instance
                if (organization != null) {
                    FXOrganization.setOrganizationOnceExpectedFieldsAreLoaded(organization);
                } else { // Otherwise, we request the server to load that organization from that id
                    organizationStore.<Organization>executeQueryWithCache("modality/crm/backoffice/fx-organization",
                            "select " + FXOrganization.EXPECTED_FIELDS + " from Organization where id=$1", organizationId)
                        .onFailure(Console::log)
                        .inUiThread()
                        .onCacheAndOrSuccess(list -> { // on successfully receiving the list (should be a singleton list)
                            if (Objects.equals(organizationId, getOrganizationId())) { // final check it is still relevant
                                Organization loadedOrganization = list.isEmpty() ? null : list.get(0);
                                FXOrganization.setOrganization(loadedOrganization); // we finally set FXEvent
                            }
                        });
                }
            }
        }
        // Passing organizationId to AuthorizationContext. This will cause a reevaluation of the authorizations, because
        // some may be granted only to a specific organization.
        AuthorizationContext.setContextProperty("organizationId", Strings.toString(organizationPrimaryKey));
    });

    static {
        // Initializing the organizationId from the last value stored in the session
        FXProperties.runNowAndOnPropertyChange(session -> {
            Object primaryKey = session == null ? null : session.get(SESSION_ORGANIZATION_ID_KEY);
            setOrganizationId(primaryKey == null ? null : EntityId.create(Organization.class, primaryKey));
        }, FXSession.sessionProperty());
    }

    static void init() {
        // Do nothing, but this ensures that the static initializer above has been called
    }

    public static ObjectProperty<EntityId> organizationIdProperty() {
        return organizationIdProperty;
    }

    public static EntityId getOrganizationId() {
        return organizationIdProperty.get();
    }

    public static void setOrganizationId(EntityId organizationId) {
        if (!Objects.equals(organizationId, getOrganizationId()))
            organizationIdProperty.set(organizationId);
    }

}
