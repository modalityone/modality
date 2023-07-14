package one.modality.event.frontoffice.activities.booking;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.states.BookingPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.activities.booking.views.CenterDisplayView;
import one.modality.event.frontoffice.activities.booking.views.EventView;
import one.modality.event.frontoffice.activities.booking.views.SearchBarView;

import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class BookingActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private VBox container = new VBox();
    private VBox nktEventsContainer = new VBox();
    private VBox localEventsContainer = new VBox();
    private VBox centerContainer = new VBox();
    private VBox searchContainer = new VBox();

    private VBox bookingWelcome = new VBox();
    private Node bookingSteps = BookingStepAll.createPage(this);
    private VBox bookingConfirmed = new VBox();

    private void rebuildEvents(VBox container, ObservableList<Event> events, boolean isSearched) {
        container.getChildren().removeAll(container.getChildren());
        container.getChildren().addAll(events.stream().map(e -> new EventView(e).getView(isSearched ? 10.0 : -1.0)).collect(Collectors.toList()));
        container.setSpacing(5);
    }

    private void rebuildCenterDisplay() {
        centerContainer.getChildren().clear();
        centerContainer.getChildren().add(new CenterDisplayView().getView(container, this,this));
    }

    public void rebuildSearchBar() {
        searchContainer.getChildren().clear();
        searchContainer.getChildren().add(new SearchBarView().getView());
    }

    public void rebuild() {
        container.getChildren().removeAll(container.getChildren());
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Text t = TextUtility.getMainHeaderText("YOUR NEXT MEANINGFUL EVENT IS HERE");
        t.setWrappingWidth(350);
        t.setTextAlignment(TextAlignment.CENTER);
        header.getChildren().add(t);

        rebuildEvents(nktEventsContainer, FXBooking.nktEvents, false);
        rebuildEvents(localEventsContainer, FXBooking.localCenterEvents, false);
        rebuildCenterDisplay();
        rebuildSearchBar();

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
                centerContainer,
                GeneralUtility.createSpace(50),
                searchContainer,
                GeneralUtility.createSpace(20),
                localEventsContainer,
                bookingWelcome);
    }

    @Override
    public Node buildUi() {
        rebuild();

        FXBooking.nktEvents.addListener((ListChangeListener<Event>) change -> rebuildEvents(nktEventsContainer, FXBooking.nktEvents, false));
        FXBooking.localCenterEvents.addListener((ListChangeListener<Event>) change -> rebuildEvents(localEventsContainer, FXBooking.localCenterEvents, false));
        BookingPM.CHANGE_CENTER.addListener(change -> rebuild());
        FXBooking.displayCenterProperty.addListener(c -> rebuild());
        I18n.dictionaryProperty().addListener(change -> rebuild());

        container.setBackground(Background.fill(Color.WHITE));

        FXProperties.runOnPropertiesChange(() -> GeneralUtility.screenChangeListened(container.getWidth()), container.widthProperty());

        return LayoutUtil.createVerticalScrollPane(container);
    }

    public void goToBookingConfirmed() {
        container.getChildren().remove(bookingSteps);
        container.getChildren().add(bookingConfirmed);
    }

    protected void startLogic() {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name, label.<loadAll>, startDate, endDate', where: 'organization.type.code = `CORP` and endDate > now()', orderBy: 'startDate'}")
                .storeEntitiesInto(FXBooking.nktEvents)
                .start();

        ReactiveEntitiesMapper.<Event>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name, label.<loadAll>, startDate, endDate', where: 'endDate > now()', orderBy: 'startDate'}")
                .ifNotNullOtherwiseEmpty(FXBooking.displayCenterProperty, localCenter -> where("organization=?", localCenter))
                .storeEntitiesInto(FXBooking.localCenterEvents)
                .start();
    }
}
