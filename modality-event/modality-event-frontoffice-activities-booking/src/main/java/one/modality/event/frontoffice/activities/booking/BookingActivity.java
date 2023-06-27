package one.modality.event.frontoffice.activities.booking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Event;
import one.modality.base.frontoffice.fx.FXBooking;
import dev.webfx.extras.util.layout.LayoutUtil;

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
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class BookingActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private Node activePage = null;
    private VBox container = new VBox();
    private VBox bookingWelcome = new VBox();
    private Node bookingSteps = BookingStepAll.createPage(this);
    private VBox bookingConfirmed = new VBox();

    private void rebuildEvents(VBox container, ObservableList<Event> events) {
        container.getChildren().removeAll(container.getChildren());
        container.getChildren().addAll(events.stream().map(this::createEventBanner).collect(Collectors.toList()));
        container.setSpacing(5);
    }

    private Node createEventBanner(Event event) {
        Text title = TextUtility.getMediumText(event.getName(), StyleUtility.MAIN_BLUE);
        Text subTitle = TextUtility.getText("LOWER DESCRIPTION", 10, StyleUtility.PURE_BLACK);
        Text date = TextUtility.getText(event.getStartDate().toString(), 10, StyleUtility.PURE_BLACK);
        Node location = GeneralUtility.createVList(0, 0,
                TextUtility.weight(TextUtility.getText("At Manjushri Kadampa Meditation Center", 8, StyleUtility.ELEMENT_GRAY), FontWeight.THIN),
                TextUtility.weight(TextUtility.getText("Ulverston, United Kingdom", 8, StyleUtility.ELEMENT_GRAY), FontWeight.MEDIUM)
                );
        Button book = GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, "Book now");

        book.setFont(Font.font(StyleUtility.TEXT_FAMILY, FontWeight.findByWeight(600), 9));

        title.setWrappingWidth(200);

        VBox container = new VBox();

        container.getChildren().add(
                GeneralUtility.createVList(10, 0,
                GeneralUtility.createSplitRow(title, date, 80, 0),
                GeneralUtility.createSplitRow(subTitle, new Text(""), 80, 0),
                GeneralUtility.createSplitRow(location, book, 80, 0))
        );

        container.setPadding(new Insets(20));
        container.setBackground(Background.fill(Color.web(StyleUtility.BACKGROUND_GRAY)));

        return container;
    }

    private Node createCenterDisplay() {
        EntityButtonSelector<Country> countriesButtonSelector = new EntityButtonSelector<>(
                "{class:'country', orderBy:'name'}",
                this, container, getDataSourceModel()
        );

        countriesButtonSelector.selectedItemProperty().bindBidirectional(FXAccount.ownerPM.ADDRESS_COUNTRY);

        EntityButtonSelector<Organization> centersButtonSelector = new EntityButtonSelector<>(
                "{class:'organization', orderBy:'name'}",
                this, container, getDataSourceModel()
        );

        centersButtonSelector.ifNotNullOtherwiseEmpty(countriesButtonSelector.selectedItemProperty(), country -> where("country=?", country));

        centersButtonSelector.selectedItemProperty().bindBidirectional(FXAccount.ownerPM.LOCAL_CENTER);

        VBox container = new VBox();

        container.getChildren().addAll(
                new Text("Your Country"),
                countriesButtonSelector.getButton(),
                new Text("Your Local Dharma Center"),
                centersButtonSelector.getButton()
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        return container;
    }

    @Override
    public Node buildUi() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Text t = TextUtility.getMainHeaderText("YOUR NEXT MEANINGFUL EVENT IS HERE");
        t.setWrappingWidth(350);
        t.setTextAlignment(TextAlignment.CENTER);
        header.getChildren().add(t);

        VBox nktEventsContainer = new VBox();
        VBox localEventsContainer = new VBox();

        rebuildEvents(nktEventsContainer, FXBooking.nktEvents);
        rebuildEvents(localEventsContainer, FXBooking.localCenterEvents);

        FXBooking.nktEvents.addListener((ListChangeListener<Event>) change -> rebuildEvents(nktEventsContainer, FXBooking.nktEvents));
        FXBooking.localCenterEvents.addListener((ListChangeListener<Event>) change -> rebuildEvents(localEventsContainer, FXBooking.localCenterEvents));



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

        container.getChildren().addAll(
                header,
                nktEventsContainer,
                GeneralUtility.createSpace(50),
                createCenterDisplay(),
                GeneralUtility.createSpace(50),
                localEventsContainer,
                bookingWelcome);

        container.setBackground(Background.fill(Color.WHITE));

        return LayoutUtil.createVerticalScrollPane(container);
    }

    public void goToBookingConfirmed() {
        container.getChildren().remove(bookingSteps);
        container.getChildren().add(bookingConfirmed);
    }

    protected void startLogic() {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name, startDate, endDate', where: 'organization.type.code = `CORP` and endDate > now()', orderBy: 'startDate'}")
                .storeEntitiesInto(FXBooking.nktEvents)
                .start();

        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name, startDate, endDate', where: 'endDate > now()', orderBy: 'startDate'}")
                .ifNotNullOtherwiseEmpty(FXBooking.ownerLocalCenterProperty, localCenter -> where("organization=?", localCenter))
                .storeEntitiesInto(FXBooking.localCenterEvents)
                .start();
    }
}
