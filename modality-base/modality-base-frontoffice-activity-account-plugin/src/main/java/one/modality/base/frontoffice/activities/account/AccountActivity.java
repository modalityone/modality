package one.modality.base.frontoffice.activities.account;

import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.authn.logout.client.operation.LogoutRequest;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.shared.entities.Document;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;


/**
 * @author Bruno Salmon
 */
final class AccountActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {

    private static final double MAX_PAGE_WIDTH = 1200; // Similar value to website

    private final ObservableList<Document> upcomingBookingsFeed = FXCollections.observableArrayList();
    private final ObservableList<Document> pastBookingsFeed = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> loadPastEventsBeforeDateProperty = new SimpleObjectProperty<>();

    @Override
    public Node buildUi() {
        Label upcomingBookingsLabel = GeneralUtility.createLabel("YourUpcomingBookings", StyleUtility.MAIN_ORANGE_COLOR);
        upcomingBookingsLabel.setContentDisplay(ContentDisplay.TOP);
        upcomingBookingsLabel.setTextAlignment(TextAlignment.CENTER);
        upcomingBookingsLabel.setPadding(new Insets(25, 0, 40, 0));
        //upcomingBookingsLabel.setBorder(new Border(new BorderStroke(StyleUtility.MAIN_ORANGE_COLOR, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 1, 0), null)));
        //VBox.setMargin(upcomingBookingsLabel, new Insets(0, 0, 10, 0));
        VBox upcomingBookingsContainer = new VBox(10);

        Label pastBookingsLabel = GeneralUtility.createLabel("YourPastBookings", StyleUtility.MAIN_ORANGE_COLOR);
        pastBookingsLabel.setContentDisplay(ContentDisplay.TOP);
        pastBookingsLabel.setTextAlignment(TextAlignment.CENTER);
        pastBookingsLabel.setPadding(new Insets(25, 0, 40, 0));
        //pastBookingsLabel.setBorder(new Border(new BorderStroke(StyleUtility.MAIN_ORANGE_COLOR, BorderStrokeStyle.SOLID, null, new BorderWidths(1, 0, 1, 0), null)));
        //VBox.setMargin(pastBookingsLabel, new Insets(10, 0, 10, 0));
        VBox pastBookingsContainer = new VBox(10);

        upcomingBookingsFeed.addListener((InvalidationListener) observable -> {
            upcomingBookingsFeed.stream().collect(Collectors.groupingBy(Document::getEvent, LinkedHashMap::new, Collectors.toList())) // Using LinkedHashMap to keep the sort
                .forEach((event, eventBookings) -> {
                    EventBookingsView eventBookingsView = new EventBookingsView(event, eventBookings);
                    upcomingBookingsContainer.getChildren().add(0, eventBookingsView.getView()); // inverting order => chronological order
                });
        });

        pastBookingsFeed.addListener((InvalidationListener) observable -> {
            pastBookingsFeed.stream().collect(Collectors.groupingBy(Document::getEvent, LinkedHashMap::new, Collectors.toList())) // Using LinkedHashMap to keep the sort
                .forEach((event, eventBookings) -> {
                    EventBookingsView eventBookingsView = new EventBookingsView(event, eventBookings);
                    pastBookingsContainer.getChildren().add(eventBookingsView.getView());
                });
        });

        Hyperlink logoutLink = ActionBinder.bindButtonToAction(new Hyperlink(), newOperationAction(LogoutRequest::new));
        VBox.setMargin(logoutLink, new Insets(50));
        VBox vBox = new VBox(
            upcomingBookingsLabel,
            upcomingBookingsContainer,
            pastBookingsLabel,
            pastBookingsContainer,
            //GeneralUtility.createOrangeLineSeparator(),
            logoutLink
        );
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setMaxWidth(MAX_PAGE_WIDTH);
        vBox.setBackground(Background.fill(Color.WHITE));

        FXProperties.runOnPropertiesChange(() -> {
            double width = vBox.getWidth();
            double fontFactor = GeneralUtility.computeFontFactor(width);
            GeneralUtility.setLabeledFont(upcomingBookingsLabel, StyleUtility.TEXT_FAMILY, FontWeight.BOLD, fontFactor * 16);
            GeneralUtility.setLabeledFont(pastBookingsLabel, StyleUtility.TEXT_FAMILY, FontWeight.BOLD, fontFactor * 16);
            GeneralUtility.setLabeledFont(logoutLink, StyleUtility.TEXT_FAMILY, FontWeight.BOLD, fontFactor * 16);
        }, vBox.widthProperty());

        ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(new BorderPane(vBox));

        // Lazy loading when the user scrolls down
        double lazyLoadingBottomSpace = Screen.getPrimary().getVisualBounds().getHeight();
        vBox.setPadding(new Insets(0, 0, lazyLoadingBottomSpace, 0));
        FXProperties.runOnPropertiesChange(() -> {
            if (ControlUtil.computeScrollPaneVBottomOffset(scrollPane) > vBox.getHeight() - lazyLoadingBottomSpace) {
                Document lastBooking = Collections.last(pastBookingsFeed);
                if (lastBooking == null)
                    vBox.setPadding(Insets.EMPTY);
                else {
                    LocalDate startDate = lastBooking.getEvent().getStartDate();
                    FXProperties.setIfNotEquals(loadPastEventsBeforeDateProperty, startDate);
                }
            }
        }, scrollPane.vvalueProperty(), vBox.heightProperty());

        return scrollPane;
    }

    @Override
    protected void startLogic() {
        // Upcoming bookings
        ReactiveEntitiesMapper.<Document>createReactiveChain(this)
            .always("{class: 'Document', alias: 'd', orderBy: 'event.startDate desc, ref desc'}")
            .always(DqlStatement.fields(BookingView.BOOKING_REQUIRED_FIELDS))
            .always(DqlStatement.where("event.endDate >= now()"))
            .always(DqlStatement.where("!cancelled or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("person.frontendAccount=?", mup.getUserAccountId()))
            .storeEntitiesInto(upcomingBookingsFeed)
            .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-account-upcomingBookings"))
            .start();

        // Past bookings
        ReactiveEntitiesMapper.<Document>createReactiveChain(this)
            .always("{class: 'Document', alias: 'd', orderBy: 'event.startDate desc, ref desc', limit: 5}")
            .always(DqlStatement.fields(BookingView.BOOKING_REQUIRED_FIELDS))
            .always(DqlStatement.where("event.endDate < now()"))
            .always(DqlStatement.where("!cancelled or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("person.frontendAccount=?", mup.getUserAccountId()))
            .ifNotNull(loadPastEventsBeforeDateProperty, date -> DqlStatement.where("event.startDate < ?", date))
            .storeEntitiesInto(pastBookingsFeed)
            .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-account-pastBookings"))
            .start();
    }

    /* Commented old Tyler implementation
    @Override
    public Node buildUi() {
        Node avatar = AccountUtility.createAvatar();

        Hyperlink logoutLink = ActionBinder.bindButtonToAction(new Hyperlink(), newOperationAction(LogoutRequest::new));
        logoutLink.setGraphicTextGap(10);
        VBox.setMargin(logoutLink, new Insets(10));

        VBox vBox = new VBox(
                new HBox(LayoutUtil.createHGrowable(), avatar, LayoutUtil.createHGrowable()),
                createRow("PersonalInformation",
                        "EditYourPersonalInformation",
                        SVGPaths.PERSONAL_INFORMATION_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("FamilyOrFriends",
                        "AddFamilyOrFriends",
                        SVGPaths.FAMILY_FRIENDS_SVG_PATH,
                        () -> new RouteToAccountFriendsAndFamilyRequest(getHistory())
                ),
                createRow("Messages",
                        "SupportMessages",
                        SVGPaths.MESSAGES_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("WalletPayments",
                        "YourWalletPayments",
                        SVGPaths.PAYMENT_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("Settings",
                        "",
                        SVGPaths.SETTINGS_SVG_PATH,
                        () -> new RouteToAccountSettingsRequest(getHistory())
                ),
                createRow("Help",
                        "",
                        SVGPaths.HELP_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                createRow("Legal",
                        "PrivacyNotice",
                        SVGPaths.LEGAL_SVG_PATH,
                        () -> new RouteToAccountPersonalInformationRequest(getHistory())
                ),
                logoutLink
        );

        vBox.setMaxWidth(Region.USE_PREF_SIZE);
        return ControlUtil.createScalableVerticalScrollPane(vBox, false);
    }

    private Node createRow(String title, String subtitle, String svgPath, Supplier<RoutePushRequest> requestSupplier) {
        Node icon = GeneralUtility.createSVGIcon(svgPath);
        Label titleLabel = I18nControls.bindI18nProperties(new Label(), title);
        titleLabel.setWrapText(true);
        Label subtitleLabel = I18nControls.bindI18nProperties(new Label(), subtitle);
        subtitleLabel.setWrapText(true);

        subtitleLabel.setOpacity(0.3d);

        Node row = GeneralUtility.createHList(10, 10,
                icon, GeneralUtility.createVList(2, 0, titleLabel, subtitleLabel)
        );

        row.setOnMouseClicked(e -> executeOperation(requestSupplier.get()));

        return row;
    }*/

}
