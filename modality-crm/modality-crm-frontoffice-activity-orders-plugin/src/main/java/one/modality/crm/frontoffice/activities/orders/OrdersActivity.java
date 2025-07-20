package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Document;
import one.modality.crm.frontoffice.order.OrderStatus;
import one.modality.crm.frontoffice.order.OrderCardView;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author David Hello
 */
final class OrdersActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final ObservableList<Document> upcomingBookingsFeed = FXCollections.observableArrayList();
    private final ObservableList<Document> pastBookingsFeed = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> loadPastEventsBeforeDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Object> selectedOrderIdProperty = new SimpleObjectProperty<>();

    @Override
    protected void updateModelFromContextParameters() {
        selectedOrderIdProperty.setValue(getParameter("documentId"));
    }

    @Override
    public Node buildUi() {
        Label ordersLabel = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(OrdersI18nKeys.OrdersTitle)));
        ordersLabel.setTextAlignment(TextAlignment.CENTER);
        ordersLabel.setWrapText(true);
        ordersLabel.setPadding(new Insets(100, 0, 40, 0));


        Label ordersExplationLabel = Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(OrdersI18nKeys.OrdersTitleExplanation)));
        ordersExplationLabel.setTextAlignment(TextAlignment.CENTER);
        ordersExplationLabel.setWrapText(true);
        ordersExplationLabel.setPadding(new Insets(0, 15, 40, 15));

        Label activeOrdersLabel = Bootstrap.strong(Bootstrap.textSuccess(Bootstrap.h4(I18nControls.newLabel(OrdersI18nKeys.ActiveOrders))));
        activeOrdersLabel.setContentDisplay(ContentDisplay.TOP);
        activeOrdersLabel.setTextAlignment(TextAlignment.CENTER);
        activeOrdersLabel.setPadding(new Insets(0, 0, 40, 0));

        VBox activeOrdersContainer = createBookingsContainer();

        Label completedOrdersLabel = Bootstrap.strong(Bootstrap.textSecondary(Bootstrap.h4(I18nControls.newLabel(OrdersI18nKeys.CompletedOrders))));
        completedOrdersLabel.setTextAlignment(TextAlignment.CENTER);
        completedOrdersLabel.setPadding(new Insets(100, 0, 40, 0));

        VBox pastBookingsContainer = createBookingsContainer();

        upcomingBookingsFeed.addListener((InvalidationListener) observable -> {
            upcomingBookingsFeed.stream().collect(Collectors.groupingBy(Document::getEvent, LinkedHashMap::new, Collectors.toList())) // Using LinkedHashMap to keep the sort
                .forEach((event, eventBookings) -> {
                    OrdersView ordersView = new OrdersView(eventBookings, selectedOrderIdProperty);
                    activeOrdersContainer.getChildren().add(0, ordersView.getView()); // inverting order => chronological order
                });
        });

        pastBookingsFeed.addListener((InvalidationListener) observable -> {
            pastBookingsFeed.stream().collect(Collectors.groupingBy(Document::getEvent, LinkedHashMap::new, Collectors.toList())) // Using LinkedHashMap to keep the sort
                .forEach((event, eventBookings) -> {
                    OrdersView ordersView = new OrdersView(eventBookings, selectedOrderIdProperty);
                    pastBookingsContainer.getChildren().add(ordersView.getView());
                });
        });

        VBox pageContainer = new VBox(
            ordersLabel,
            ordersExplationLabel,
            activeOrdersLabel,
            activeOrdersContainer,
            completedOrdersLabel,
            pastBookingsContainer
        );
        pageContainer.setAlignment(Pos.TOP_CENTER);


        FXProperties.runOnPropertyChange(activeOrdersContainer.getChildren()::clear, FXModalityUserPrincipal.modalityUserPrincipalProperty());
        // Lazy loading when the user scrolls down
        Controls.onScrollPaneAncestorSet(pageContainer, scrollPane -> {
            double lazyLoadingBottomSpace = Screen.getPrimary().getVisualBounds().getHeight();
            pageContainer.setPadding(new Insets(0, 0, lazyLoadingBottomSpace, 0));
            FXProperties.runOnPropertiesChange(() -> {
                if (Controls.computeScrollPaneVBottomOffset(scrollPane) > pageContainer.getHeight() - lazyLoadingBottomSpace) {
                    Document lastBooking = Collections.last(pastBookingsFeed);
                    if (lastBooking == null)
                        pageContainer.setPadding(Insets.EMPTY);
                    else {
                        LocalDate startDate = lastBooking.getEvent().getStartDate();
                        FXProperties.setIfNotEquals(loadPastEventsBeforeDateProperty, startDate);
                    }
                }
            }, scrollPane.vvalueProperty(), pageContainer.heightProperty());
        });

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageTopBottomPadding(pageContainer);
    }

    private static VBox createBookingsContainer() {
        VBox ordersContainer = new VBox(30);
        ordersContainer.setBackground(BackgroundFactory.newBackground(Color.gray(0.95), 30));
        ordersContainer.setPadding(new Insets(40, 0, 40, 0));
        return ordersContainer;
    }

    @Override
    protected void startLogic() {
        // Upcoming bookings
        ReactiveEntitiesMapper.<Document>createReactiveChain(this)
            .always("{class: 'Document', alias: 'd'}")
            .always(DqlStatement.fields(OrderCardView.ORDER_REQUIRED_FIELDS))
            .always(where("event.endDate >= now()"))
            .always(where("!abandoned or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("person.frontendAccount=?", mup.getUserAccountId()))
            .always(orderBy(OrderStatus.getBookingStatusOrderExpression(true)))
            .always(orderBy("event.startDate desc, ref desc"))
            .storeEntitiesInto(upcomingBookingsFeed)
            .start();

        // Past bookings
        ReactiveEntitiesMapper.<Document>createReactiveChain(this)
            .always("{class: 'Document', alias: 'd', orderBy: 'event.startDate desc, ref desc', limit: 5}")
            .always(DqlStatement.fields(OrderCardView.ORDER_REQUIRED_FIELDS))
            .always(where("event.endDate < now()"))
            .always(where("!abandoned or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("person.frontendAccount=?", mup.getUserAccountId()))
            .ifNotNull(loadPastEventsBeforeDateProperty, date -> where("event.startDate < ?", date))
            .storeEntitiesInto(pastBookingsFeed)
            .start();
    }

}
