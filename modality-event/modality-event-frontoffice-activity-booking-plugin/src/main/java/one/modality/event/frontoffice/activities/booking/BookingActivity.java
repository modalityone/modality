package one.modality.event.frontoffice.activities.booking;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.GrowingPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.os.OperatingSystem;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ObservableEntitiesToObjectsMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.css.Fonts;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.frontoffice.activities.booking.views.EventView;
import one.modality.event.frontoffice.activities.booking.views.OrganizationSelectorView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

final class BookingActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final VBox internationalEventsContainer = new VBox(20);
    private final OrganizationSelectorView organizationSelectorView = new OrganizationSelectorView(this, this);
    private final VBox localEventsContainer = new VBox(20);
    private final TabsBar<Entity> tabsBar = new TabsBar<>(this, this::showLocalEventsOfType);
    private final FlexPane localEventTypeTabsPane = new FlexPane();
    private final ObservableList<Event> localEvents = FXCollections.observableArrayList();
    private final ObservableList<Event> localEventsOfSelectedType = FXCollections.observableArrayList();

    @Override
    public Node buildUi() {
        Label headerLabel = GeneralUtility.createLabel(BookingI18nKeys.eventsHeader, Brand.getBrandMainColor());
        headerLabel.setTextAlignment(TextAlignment.CENTER);

        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.booking").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setMaxHeight(300);

        Label internationalEventsLabel = GeneralUtility.createLabel(BookingI18nKeys.InternationalEvents, Color.BLACK);

        Node localCenterDisplay = organizationSelectorView.getView();

        Label localEventsLabel = GeneralUtility.createLabel(BookingI18nKeys.localEvents, Color.BLACK);
        localEventsLabel.setVisible(false); // Using it for spacing only for now

        double mobileStatusBarHeight = OperatingSystem.isMobile() && UserAgent.isNative() ? 15 : 0;
        VBox.setMargin(headerLabel, new Insets(5 + mobileStatusBarHeight, 0, 5, 0));
        VBox.setMargin(internationalEventsLabel, new Insets(20));
        VBox.setMargin(localCenterDisplay, new Insets(10, 0, 0, 0));
        VBox.setMargin(localEventsLabel, new Insets(25));

        GrowingPane growingPane = new GrowingPane(localEventsContainer);

        VBox pageContainer = new VBox(
            headerLabel,
            headerImageScalePane,
            /* Commented for now as international events will appear on the home page
            internationalEventsLabel,
            internationalEventsContainer,
            */
            localEventsLabel,
            localCenterDisplay,
            localEventTypeTabsPane,
            growingPane);
        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.setPadding(new Insets(0, 0, 200, 0)); // Footer margin (white)

        FXProperties.runOnDoublePropertyChange(width -> {
            double fontFactor = GeneralUtility.computeFontFactor(width);
            GeneralUtility.setLabeledFont(headerLabel, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.BOLD, fontFactor * 21);
            GeneralUtility.setLabeledFont(internationalEventsLabel, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.BOLD, fontFactor * 16);
            GeneralUtility.setLabeledFont(localEventsLabel, Fonts.MONTSERRAT_TEXT_FAMILY, FontWeight.BOLD, fontFactor * 16);
        }, pageContainer.widthProperty());

        localEvents.addListener((InvalidationListener) observable -> {
            localEventsContainer.getChildren().clear();
            growingPane.reset();
            List<Entity> localEventTypes = new ArrayList<>();
            for (Event event : localEvents) {
                Entity eventType = event.getForeignEntity("type");
                if (!localEventTypes.contains(eventType))
                    localEventTypes.add(eventType);
            }
            localEventTypes.sort(Comparator.comparing(type -> type.getIntegerFieldValue("ord")));
            tabsBar.setTabs(Collections.toArray(Collections.map(localEventTypes, eventType -> {
                Tab tab = tabsBar.createTab(eventType.getStringFieldValue("name"), eventType);
                tab.setPadding(new Insets(5));
                return tab;
            }), Tab[]::new));
            localEventTypeTabsPane.getChildren().setAll(tabsBar.getTabs());
        });

        Function<Event, IndividualEntityToObjectMapper<Event, Node>>
            factory = IndividualEntityToObjectMapper.factory(EventView::new, EventView::setEvent, EventView::getView);
        ObservableEntitiesToObjectsMapper<Event, ? extends IndividualEntityToObjectMapper<Event, Node>>
            entitiesToObjectsMapper = new ObservableEntitiesToObjectsMapper<>(localEventsOfSelectedType, factory, (Event e, IndividualEntityToObjectMapper<Event, Node> m) -> m.onEntityChangedOrReplaced(e), (Event e1, IndividualEntityToObjectMapper<Event, Node> m1) -> m1.onEntityRemoved(e1));
        ObservableLists.bindConverted(localEventsContainer.getChildren(), entitiesToObjectsMapper.getMappedObjects(), IndividualEntityToObjectMapper::getMappedObject);

        return FOPageUtil.restrictToMaxPageWidthAndApplyPageTopBottomPadding(pageContainer);
        //return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, true);
    }

    private void showLocalEventsOfType(Entity eventType) {
        localEventsOfSelectedType.setAll(localEvents.filtered(e -> e.getForeignEntity("type") == eventType));
    }

    @Override
    public void onResume() {
        super.onResume();
        organizationSelectorView.onResume();
    }

    protected void startLogic() {
        String commonDqlStart = "{class: 'Event', alias: 'e', fields:'name, label.<loadAll>, kbs3, live, openingDate, startDate, endDate, organization, organization.country, venue.(name, label.<loadAll>, country), host, frontend, image.url, shortDescriptionLabel.<loadAll>";

        // Loading international Festivals
        ReactiveObjectsMapper.<Event, Node>createPushReactiveChain(this)
            .always(commonDqlStart + "', where: 'organization.type.code = `CORP` and endDate > now() and !bookingClosed and name not like `%Online%`', orderBy: 'startDate, id'}")
            .always(DqlStatement.where("name like '%Festival%'")) // This is to remove STTP classes TODO: find a more generic way to filter out private events
            .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.factory(EventView::new, EventView::setEvent, EventView::getView))
            .storeMappedObjectsInto(internationalEventsContainer.getChildren())
            .setResultCacheEntry("modality/event/booking/international-events")
            .start();

        // Loading local events
        ReactiveObjectsMapper.<Event, Node>createPushReactiveChain(this)
            .always(commonDqlStart + ", type.(name,label.<loadAll>,ord), state', where: 'endDate > now() and !bookingClosed and type.name != `Not event`', orderBy: 'startDate'}")
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), orgId -> where("organization=?", orgId))
            .storeEntitiesInto(localEvents)
            .setResultCacheEntry("modality/event/booking/local-events")
            .start();
    }
}
