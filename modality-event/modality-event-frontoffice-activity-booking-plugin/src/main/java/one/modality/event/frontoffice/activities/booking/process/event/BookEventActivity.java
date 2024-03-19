package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.text.Text;
import one.modality.event.frontoffice.activities.booking.fx.FXEventAggregate;

/**
 * @author Bruno Salmon
 */
public final class BookEventActivity extends ViewDomainActivityBase {

    @Override
    public Node buildUi() {
        return new Text("Recurring Event");
    }

    @Override
    protected void updateContextParametersFromRoute() {
        super.updateContextParametersFromRoute();
    }

    @Override
    protected void updateModelFromContextParameters() {
        super.updateModelFromContextParameters();
    }

    @Override
    protected void startLogic() {
        FXEventAggregate.getEventAggregate().load()
                .onFailure(Console::log)
                .onSuccess(ignored -> {
                    Console.log(FXEventAggregate.getEventAggregate().getScheduledItems());
                });
    }
}
