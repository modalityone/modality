package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.background.BackgroundFactory;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Document;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.ecommerce.frontoffice.order.OrderCard;
import one.modality.ecommerce.frontoffice.order.OrderStatus;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 * @author David Hello
 */
final class OrdersActivity extends ViewDomainActivityBase implements ModalityButtonFactoryMixin {

    private final ObservableList<OrderCard> upcomingOrderCards = FXCollections.observableArrayList();
    private final ObservableList<Document> pastOrdersFeed = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> loadPastEventsBeforeDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Object> selectedOrderIdProperty = new SimpleObjectProperty<>();
    private ReactiveObjectsMapper<Document, OrderCard> upcomingOrdersMapper;
    private ReactiveEntitiesMapper<Document> completedOrdersMapper;

    @Override
    protected void updateModelFromContextParameters() {
        // If the route request comes after an order modification (ex: StepCThankYouSlide), the /refresh suffix has been
        // added and then interpreted by ModalityClientStarterActivity as a "refresh" = true parameter, indicating that
        // the selected order must be refreshed even if it's identical to the last one visited.
        if (Booleans.booleanValue(getParameter("refresh"))) // if "refresh" = true
            selectedOrderIdProperty.setValue(null); // we reset selectedOrderIdProperty to force the change
        // We set selectedOrderIdProperty with the route parameter "documentId" (if provided)
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

        VBox activeOrdersContainer = createOrdersContainer();
        // No feed for the active orders, we just load them all, map them to cards and then add them to the container,
        // but keeping the initial progress indicator until the first result arrives.
        FXProperties.runNowAndOnPropertyChange(calling -> UiScheduler.scheduleDeferred(() -> {
            if (calling)
                activeOrdersContainer.getChildren().setAll(Controls.createProgressIndicator(50));
            else if (upcomingOrderCards.isEmpty())
                activeOrdersContainer.getChildren().setAll(Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(OrdersI18nKeys.NoActiveOrders))));
            else
            {
                ObservableLists.setAllConverted(upcomingOrderCards, OrderCard::getView, activeOrdersContainer.getChildren());
                for (OrderCard orderCard : upcomingOrderCards) {
                    //We expand all the upcoming orders by default
                    orderCard.expandDetails();
                }
            }
        }), upcomingOrdersMapper.callingProperty());

        Label completedOrdersLabel = Bootstrap.strong(Bootstrap.textSecondary(Bootstrap.h4(I18nControls.newLabel(OrdersI18nKeys.CompletedOrders))));
        completedOrdersLabel.setTextAlignment(TextAlignment.CENTER);
        completedOrdersLabel.setPadding(new Insets(100, 0, 40, 0));

        VBox pastOrdersContainer = createOrdersContainer();
        // Feed management for the past orders
        pastOrdersFeed.addListener((InvalidationListener) observable -> {
            pastOrdersFeed.stream().collect(Collectors.groupingBy(Document::getEvent, LinkedHashMap::new, Collectors.toList())) // Using LinkedHashMap to keep the sort
                .forEach((event, eventOrders) -> {
                    eventOrders.forEach(orderDocument -> {
                        OrderCard orderCard = new OrderCard(orderDocument);
                        // Inserting the card before the progress indicator (which is normally the last child)
                        pastOrdersContainer.getChildren().add(pastOrdersContainer.getChildren().size() - 1, orderCard.getView());
                    });
                });
        });
        FXProperties.runNowAndOnPropertyChange(calling -> UiScheduler.scheduleDeferred(() -> {
            if (calling && pastOrdersContainer.getChildren().isEmpty())
                pastOrdersContainer.getChildren().setAll(Controls.createProgressIndicator(50));
            else if (!calling && pastOrdersContainer.getChildren().size() == 1)
                pastOrdersContainer.getChildren().setAll(Bootstrap.strong(Bootstrap.textSecondary(I18nControls.newLabel(OrdersI18nKeys.NoCompletedOrders))));
        }), completedOrdersMapper.callingProperty());

        VBox pageContainer = new VBox(
            ordersLabel,
            ordersExplationLabel,
            activeOrdersLabel,
            activeOrdersContainer,
            completedOrdersLabel,
            pastOrdersContainer
        );
        pageContainer.setAlignment(Pos.TOP_CENTER);

        FXProperties.runOnPropertyChange(() -> UiScheduler.runInUiThread(() -> {
            activeOrdersContainer.getChildren().clear();
            pastOrdersContainer.getChildren().clear();
        }), FXModalityUserPrincipal.modalityUserPrincipalProperty());

        // Lazy loading when the user scrolls down
        Controls.onScrollPaneAncestorSet(pageContainer, scrollPane -> {
            double lazyLoadingBottomSpace = Screen.getPrimary().getVisualBounds().getHeight();
            pageContainer.setPadding(new Insets(0, 0, lazyLoadingBottomSpace, 0));
            FXProperties.runOnPropertiesChange(() -> {
                if (Controls.computeScrollPaneVBottomOffset(scrollPane) > pageContainer.getHeight() - lazyLoadingBottomSpace) {
                    Document lastOrderDocument = Collections.last(pastOrdersFeed);
                    if (lastOrderDocument != null) {
                        LocalDate startDate = lastOrderDocument.getEvent().getStartDate();
                        FXProperties.setIfNotEquals(loadPastEventsBeforeDateProperty, startDate);
                    } else if (loadPastEventsBeforeDateProperty.get() != null) {
                        // Removing the progress indicator normally present as the last child
                        if (Collections.last(pastOrdersContainer.getChildren()) instanceof ProgressIndicator)
                            pastOrdersContainer.getChildren().remove(pastOrdersContainer.getChildren().size() - 1);
                        pageContainer.setPadding(Insets.EMPTY);
                    }
                }
            }, scrollPane.vvalueProperty(), pageContainer.heightProperty());
        });

        // selectedOrderIdProperty management
        FXProperties.runNowAndOnPropertiesChange(o -> {
            Object orderId = selectedOrderIdProperty.get();
            if (orderId != null) {
                boolean found = false;
                for (OrderCard orderCard : upcomingOrderCards) {
                    if (orderCard.autoScrollToExpandedDetailsIfOrderId(orderId))
                        found = true;
                }
                if (!found && !upcomingOrderCards.isEmpty())
                    upcomingOrdersMapper.refreshWhenActive();
            }
        }, selectedOrderIdProperty, ObservableLists.versionNumber(upcomingOrderCards));

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageTopBottomPadding(pageContainer);
    }

    private static VBox createOrdersContainer() {
        VBox ordersContainer = new VBox(30);
        ordersContainer.setAlignment(Pos.TOP_CENTER);
        ordersContainer.setBackground(BackgroundFactory.newBackground(Color.gray(0.95), 30));
        ordersContainer.setPadding(new Insets(40, 30, 40, 30));
        return ordersContainer;
    }

    @Override
    protected void startLogic() {
        // Upcoming orders
        upcomingOrdersMapper = ReactiveObjectsMapper.<Document, OrderCard>createReactiveChain(this)
            .always( // language=JSON5
                "{class: 'Document', alias: 'd', fields: 'event.vodExpirationDate' }")
            .always(DqlStatement.fields(OrderCard.ORDER_REQUIRED_FIELDS))
            .always(where("event.endDate >= now() or event.vodExpirationDate >= now()"))
            .always(where("!abandoned or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("accountCanAccessPersonOrders(?, person)", mup.getUserAccountId()))
            .always(orderBy(OrderStatus.getBookingStatusOrderExpression(true)))
            .always(orderBy("event.startDate desc, ref desc"))
            .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.factory(OrderCard::new))
            .storeMappedObjectsInto(upcomingOrderCards)
            .start();

        // Past orders
        completedOrdersMapper = ReactiveEntitiesMapper.<Document>createReactiveChain(this)
            .always( // language=JSON5
                "{class: 'Document', alias: 'd', fields: 'event.vodExpirationDate', orderBy: 'event.startDate desc, ref desc', limit: 5}")
            .always(DqlStatement.fields(OrderCard.ORDER_REQUIRED_FIELDS))
            .always(where("event.endDate < now() and (event.vodExpirationDate==null or event.vodExpirationDate < now())"))
            .always(where("!abandoned or price_deposit>0"))
            .ifNotNullOtherwiseEmpty(FXModalityUserPrincipal.modalityUserPrincipalProperty(), mup -> where("accountCanAccessPersonOrders(?, person)", mup.getUserAccountId()))
            .ifNotNull(loadPastEventsBeforeDateProperty, date -> where("event.startDate < ?", date))
            .storeEntitiesInto(pastOrdersFeed)
            .start();
    }
}
