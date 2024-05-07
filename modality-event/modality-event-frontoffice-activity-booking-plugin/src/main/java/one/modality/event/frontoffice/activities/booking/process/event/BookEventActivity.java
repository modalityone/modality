package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.scene.Node;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.fx.FXEventAggregate;
import one.modality.event.client.event.fx.FXEventId;
import one.modality.event.frontoffice.activities.booking.process.EventAggregate;

/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return I18n.bindI18nProperties(new Text(), "Recurring event: {0}", FXEventId.eventIdProperty());
    }

    @Override
    protected void updateContextParametersFromRoute() {
        super.updateContextParametersFromRoute();
    }

    @Override
    protected void updateModelFromContextParameters() {
        Object eventId = Numbers.toShortestNumber((Object) getParameter("eventId"));
        FXEventId.setEventId(EntityId.create(Event.class, eventId));
    }

    @Override
    protected void startLogic() {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            EventAggregate eventAggregate = FXEventAggregate.getEventAggregate();
            if (eventAggregate != null) {
                eventAggregate.load()
                        .onFailure(Console::log)
                        .onSuccess(ignored -> {
                            Console.log(eventAggregate.getScheduledItems());
                        });
            }
        }, FXEventAggregate.eventAggregateProperty());
    }
}
