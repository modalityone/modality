package one.modality.event.frontoffice.activities.booking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.base.frontoffice.fx.FXBooking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.states.AccountHomePM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

import java.util.stream.Collectors;

public class BookingActivity extends ViewDomainActivityBase {
    private Node activePage = null;
    private VBox container = new VBox();
    private VBox bookingWelcome = new VBox();
    private Node bookingSteps = BookingStepAll.createPage(this);
    private VBox bookingConfirmed = new VBox();

    private void rebuildEvents(VBox container) {
        container.getChildren().removeAll(container.getChildren());
        container.getChildren().addAll(FXBooking.nktEvents.stream().map(o -> new Text(o.getName())).collect(Collectors.toList()));
    }

    @Override
    public Node buildUi() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Text t = TextUtility.getMainHeaderText("YOUR NEXT MEANINGFUL EVENT IS HERE");
        t.setWrappingWidth(350);
        t.setTextAlignment(TextAlignment.CENTER);
        header.getChildren().add(t);

        VBox eventsContainer = new VBox();

        rebuildEvents(eventsContainer);

        FXBooking.nktEvents.addListener((ListChangeListener<Event>) change -> rebuildEvents(eventsContainer));

        Button startBooking = new Button("Start Booking");
        bookingWelcome.getChildren().addAll(
                startBooking
        );

        startBooking.setOnAction(e -> {
            container.getChildren().remove(bookingWelcome);
            container.getChildren().add(bookingSteps);
        });

        Button restartBooking = new Button("Restart");

        restartBooking.setOnAction(e -> {
            container.getChildren().remove(bookingConfirmed);
            container.getChildren().add(bookingWelcome);
            BookingStepAll.step10.go(BookingStepAll.step1);
        });

        bookingConfirmed.getChildren().addAll(
                new Text("How wonderful! Let's Festival!"),
                restartBooking
        );

        container.getChildren().addAll(header, eventsContainer, bookingWelcome);

        return container;
    }

    public void goToBookingConfirmed() {
        container.getChildren().remove(bookingSteps);
        container.getChildren().add(bookingConfirmed);
    }

    protected void startLogic() {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name', where: 'organization.type.code = `CORP` and endDate > now()', orderBy: 'startDate'}")
                .storeEntitiesInto(FXBooking.nktEvents)
                .start();

        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name', where: 'endDate > now()', orderBy: 'startDate'}")
                .ifNotNullOtherwiseEmpty(FXBooking.ownerLocalCenterProperty, localCenter -> DqlStatement.where("organization=?", localCenter))
                .storeEntitiesInto(FXBooking.localCenterEvents)
                .start();
    }
}
