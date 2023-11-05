package one.modality.event.backoffice.event.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.session.Session;
import dev.webfx.stack.session.SessionService;
import dev.webfx.stack.session.state.client.fx.FXSession;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Event;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXEventId {

    private final static String SESSION_FX_EVENT_ID_KEY = "fxEventId";

    private final static ObjectProperty<EntityId> eventIdProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            // Getting the eventId that just changed
            EntityId eventId = getEventId();
            // Storing this new value (more precisely the primary key) in the session, and save it
            Session session = FXSession.getSession();
            if (session != null) {
                session.put(SESSION_FX_EVENT_ID_KEY, Entities.getPrimaryKey(eventId));
                SessionService.getSessionStore().put(session);
            }
            // Synchronizing FXOrganization to match that new organization id (FXOrganizationId => FXOrganization)
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
                    else // Otherwise, we request the server to load that event from that id
                        eventStore
                                .<Event>executeQuery("select icon,name,startDate,endDate from Event where id=?", eventId)
                                .onFailure(Console::log)
                                .onSuccess(list -> // on successfully receiving the list (should be a singleton list)
                                        FXEvent.setEvent(list.isEmpty() ? null : list.get(0))); // we finally set FXOrganization
                }
            }
        }
    };

    static {
        // Initializing the organizationId from the last value stored in the session
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Session session = FXSession.getSession();
            Object primaryKey = session == null ? null : session.get(SESSION_FX_EVENT_ID_KEY);
            setEventId(EntityId.create(Event.class, primaryKey));
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
