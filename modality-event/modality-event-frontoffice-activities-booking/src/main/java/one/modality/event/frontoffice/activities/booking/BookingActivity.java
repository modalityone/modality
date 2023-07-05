package one.modality.event.frontoffice.activities.booking;

import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import one.modality.base.frontoffice.fx.FXAccount;
import one.modality.base.frontoffice.fx.FXBooking;
import one.modality.base.frontoffice.states.BookingPM;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;

import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class BookingActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private Node activePage = null;
    private VBox container = new VBox();
    private VBox bookingWelcome = new VBox();
    private Node bookingSteps = BookingStepAll.createPage(this);
    private VBox bookingConfirmed = new VBox();

    private void rebuildEvents(VBox container, ObservableList<Event> events, boolean isSearched) {
        container.getChildren().removeAll(container.getChildren());
        container.getChildren().addAll(events.stream().map(e -> createEventBanner(e, isSearched ? 10.0 : -1.0)).collect(Collectors.toList()));
        container.setSpacing(5);
    }

    private Node createEventBanner(Event event, Double distance) {
        Object language = I18n.getLanguage();
        String titleStr = (event.getLabel() != null) ? event.getLabel().getStringFieldValue(language) : event.getName();
        titleStr = (titleStr == null) ? event.getName() : titleStr;
        Text title = TextUtility.getMediumText(titleStr, StyleUtility.MAIN_BLUE);
        Text subTitle = TextUtility.getText("LOWER DESCRIPTION", 10, StyleUtility.VICTOR_BATTLE_BLACK);
        Text date = TextUtility.getText(event.getStartDate().toString(), 10, StyleUtility.VICTOR_BATTLE_BLACK);
        Node location = GeneralUtility.createVList(0, 0,
                TextUtility.weight(TextUtility.getText("At Manjushri Kadampa Meditation Center", 8, StyleUtility.ELEMENT_GRAY), FontWeight.THIN),
                TextUtility.weight(TextUtility.getText("Ulverston, United Kingdom", 8, StyleUtility.ELEMENT_GRAY), FontWeight.MEDIUM),
                distance < 0 ? new VBox() : TextUtility.getText(distance.toString(), 8, StyleUtility.ELEMENT_GRAY)
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

    private Node rebuildCenterDisplay(VBox container) {
        if (FXAccount.ownerPM.PERSON == null) return new VBox();

        try {
            container.getChildren().removeAll(container.getChildren());
        } catch (IllegalStateException e) {
            container = new VBox();
        }

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

        WebView mapView = new WebView();
        mapView.getEngine().load("https://maps.googleapis.com/maps/api/js?key=***REMOVED***&v=3.exp");

        Image mapImage = new Image("https://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=12&size=400x400&key=AIzaSyAihoCYFho8rqJwnBjxzBlk56SR0uL7_Ks", true);
        ImageView map = new ImageView(mapImage);

        FXBooking.centerImageProperty.addListener(change -> {
            map.setImage(new Image(FXBooking.centerImageProperty.get(), true));
        });

        mapView.setMinWidth(100);
        mapView.setMinHeight(120);

        map.setFitWidth(150);
        map.setFitHeight(150);

        Text address = TextUtility.getSubText("Manjushri Kadampa Meditation Centre Conishead Priory, Ulverston LA12 9QQ", StyleUtility.RUPAVAJRA_WHITE);
        address.setWrappingWidth(100);

        Node location = GeneralUtility.createSplitRow(
                map,
                GeneralUtility.createVList(5, 0,
                        TextUtility.getSubText("manjushri.org", StyleUtility.RUPAVAJRA_WHITE),
                        address,
                        TextUtility.getSubText("+44 (0)1229 584029", StyleUtility.RUPAVAJRA_WHITE),
                        TextUtility.getSubText("info@manjushri.org", StyleUtility.RUPAVAJRA_WHITE)
                        ),50, 10
        );

        Text changeLocation = TextUtility.getText(BookingPM.CHANGE_CENTER.get() ? "Confirm change" : "Change your location", 10, StyleUtility.RUPAVAJRA_WHITE);
        changeLocation.setOnMouseClicked(event -> {
            if (BookingPM.CHANGE_CENTER.get()) FXAccount.updatePerson(FXAccount.ownerPM);
            BookingPM.CHANGE_CENTER.set(!BookingPM.CHANGE_CENTER.get());
        });
        changeLocation.setTextAlignment(TextAlignment.CENTER);

        Text localCenterText = TextUtility.getMainText(FXAccount.ownerPM.LOCAL_CENTER.getValue().getName(), StyleUtility.VICTOR_BATTLE_BLACK);
        localCenterText.setWrappingWidth(200);
        localCenterText.setTextAlignment(TextAlignment.CENTER);

        container.getChildren().addAll(
                TextUtility.getSubText("Your Country", StyleUtility.RUPAVAJRA_WHITE),
                BookingPM.CHANGE_CENTER.get() ? countriesButtonSelector.getButton() : TextUtility.getMainText(FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName(), StyleUtility.VICTOR_BATTLE_BLACK),
                GeneralUtility.createSpace(20),
                TextUtility.getSubText("Your Local Dharma Center", StyleUtility.RUPAVAJRA_WHITE),
                BookingPM.CHANGE_CENTER.get() ? centersButtonSelector.getButton() : localCenterText,
                GeneralUtility.createSpace(20),
                location,
                GeneralUtility.createSpace(20),
                changeLocation
        );

        container.setBackground(Background.fill(Color.web(StyleUtility.MAIN_BLUE)));
        container.setPadding(new Insets(35));
        container.setAlignment(Pos.CENTER);

        return container;
    }

    public Node rebuildSearchBar(VBox container) {
        try {
            container.getChildren().removeAll(container.getChildren());
        } catch (IllegalStateException exception) {
            container = new VBox();
        }

        StackPane sp = new StackPane();

        BorderPane searchContainer = new BorderPane();
        Button search = new Button("Search");
        searchContainer.setRight(search);
        searchContainer.setTop(GeneralUtility.createField("City", GeneralUtility.createBindedTextField(FXBooking.keywordsSearchProperty, -1)));

        search.setOnMouseClicked(e -> {
            FXBooking.countryProperty.set("United States");
            FXBooking.cityProperty.set("San Francisco");
            EntityStore store = FXAccount.ownerPM.PERSON.getStore();
            Organization organization = store.createEntity(Organization.class);
            organization.setName("My center");
            FXBooking.displayCenterProperty.setValue(organization);

            BookingUtility.cityAutoComplete(FXBooking.keywordsSearchProperty.get());
        });

        String locationString = FXBooking.countryProperty.get() + ", " + FXBooking.cityProperty.get();

        if (locationString.trim().equals(",")) locationString = "Please select a location";

        Text title = TextUtility.getText("Local Events", 16, StyleUtility.VICTOR_BATTLE_BLACK);
        Text location = TextUtility.getText(locationString, 8, StyleUtility.ELEMENT_GRAY);
        Text clear = TextUtility.getText("Clear filters", 8, StyleUtility.ELEMENT_GRAY);

        VBox titleContainer = (VBox) GeneralUtility.createVList(0, 0, title, location);

        clear.setOnMouseClicked(e -> {
            FXBooking.cityProperty.set(FXAccount.ownerPM.ADDRESS_CITY.get());
            FXBooking.countryProperty.set(FXAccount.ownerPM.ADDRESS_COUNTRY.getValue().getName());
            FXBooking.displayCenterProperty.setValue(FXAccount.ownerPM.LOCAL_CENTER.getValue());
        });

        titleContainer.setAlignment(Pos.CENTER);
        container.setAlignment(Pos.CENTER);

        sp.getChildren().addAll(
                titleContainer,
                searchContainer
        );

        container.getChildren().addAll(sp, clear);

        return container;
    }

    public void rebuild() {
        container.getChildren().removeAll(container.getChildren());
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        Text t = TextUtility.getMainHeaderText("YOUR NEXT MEANINGFUL EVENT IS HERE");
        t.setWrappingWidth(350);
        t.setTextAlignment(TextAlignment.CENTER);
        header.getChildren().add(t);

        VBox nktEventsContainer = new VBox();
        VBox localEventsContainer = new VBox();
        VBox centerContainer = new VBox();
        VBox searchContainer = new VBox();

        rebuildEvents(nktEventsContainer, FXBooking.nktEvents, false);
        rebuildEvents(localEventsContainer, FXBooking.localCenterEvents, false);
        rebuildCenterDisplay(centerContainer);
        rebuildSearchBar(searchContainer);

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
        FXBooking.nktEvents.addListener((ListChangeListener<Event>) change -> rebuild());
        FXBooking.localCenterEvents.addListener((ListChangeListener<Event>) change -> rebuild());
        BookingPM.CHANGE_CENTER.addListener(change -> rebuild());
        FXBooking.displayCenterProperty.addListener(c -> rebuild());
        I18n.dictionaryProperty().addListener(change -> rebuild());

        container.setBackground(Background.fill(Color.WHITE));

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
