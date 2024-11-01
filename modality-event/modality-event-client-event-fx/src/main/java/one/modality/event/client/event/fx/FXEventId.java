package one.modality.event.client.event.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.authn.login.ui.FXLoginContext;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.session.Session;
import dev.webfx.stack.session.SessionService;
import dev.webfx.stack.session.state.client.fx.FXSession;
import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.context.ModalityContext;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXEventId {

    private static final String SESSION_FX_EVENT_ID_KEY = "fxEventId";

    private final static ObjectProperty<EntityId> eventIdProperty = FXProperties.newObjectProperty(eventId -> {
        // Storing this new value (more precisely the primary key) in the session, and save it
        Session session = FXSession.getSession();
        if (session != null) {
            session.put(SESSION_FX_EVENT_ID_KEY, Entities.getPrimaryKey(eventId));
            SessionService.getSessionStore().put(session);
        }
        // Also updating the FXLoginContext
        Object loginContext = FXLoginContext.getLoginContext();
        if (loginContext instanceof ModalityContext) {
            ((ModalityContext) loginContext).setEventId(Entities.getPrimaryKey(eventId));
        }
        // Synchronizing FXEvent to match that new event id (FXEventId => FXEvent)
        if (!Objects.equals(eventId, FXEvent.getEventId())) { // Sync only if ids differ.
            // If the new event id is null, we set the FXEvent to null
            if (Entities.getPrimaryKey(eventId) == null)
                FXEvent.setEvent(null);
            else {
                // Getting the event store
                EntityStore eventStore = FXEvent.getEventStore();
                // Checking if we can find the event in memory in that store
                Event event = eventStore.getEntity(eventId);
                // If yes, there is no need to request the server, we use directly that instance
                if (event != null)
                    FXEvent.setEvent(event);
                else { // Otherwise, we request the server to load that event from that id
                    eventStore.<Event>executeQuery("select " + FXEvent.EXPECTED_FIELDS + " from Event where id=?", eventId)
                        .onFailure(Console::log)
                        .onSuccess(list -> // on successfully receiving the list (should be a singleton list)
                            UiScheduler.runInUiThread(() -> {
                                if (Objects.equals(eventId, getEventId())) { // final check it is still relevant
                                    Event loadedEvent = list.isEmpty() ? null : list.get(0);
                                    FXEvent.setEvent(loadedEvent); // we finally set FXEvent
                                    // In addition, in case FXOrganizationId is not yet set, we set it now. For ex,
                                    // if a user books an event for the first time through visiting the organization
                                    // website which redirected the booking to Modality, we memorize the organization
                                    // so that at the end of the booking process, if the users visits the Modality
                                    // booking page, he doesn't have to select the organization again, it is already
                                    // selected, and the user can see all its other events on the booking page.
                                    if (loadedEvent != null && FXOrganizationId.getOrganizationId() == null) {
                                        FXOrganizationId.setOrganizationId(loadedEvent.getOrganizationId());
                                    }
                                }
                            }));
                }
            }
        }
    });

    static {
        // Initializing the eventId from the last value stored in the session
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Session session = FXSession.getSession();
            Object primaryKey = session == null ? null : session.get(SESSION_FX_EVENT_ID_KEY);
            setEventId(primaryKey == null ? null : EntityId.create(Event.class, primaryKey));
        }, FXSession.sessionProperty());
    }

    static void init() {
        // Do nothing, but this ensures that the static initializer above has been called
    }

    public static ObjectProperty<EntityId> eventIdProperty() {
        return eventIdProperty;
    }

    public static EntityId getEventId() {
        return eventIdProperty.get();
    }

    public static void setEventId(EntityId eventId) {
        if (!Objects.equals(eventId, getEventId()))
            eventIdProperty.set(eventId);
    }

}
