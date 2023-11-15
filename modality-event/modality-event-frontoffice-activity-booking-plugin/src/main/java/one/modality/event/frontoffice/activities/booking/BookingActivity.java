package one.modality.event.frontoffice.activities.booking;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.extras.panes.FlexPane;
import dev.webfx.extras.panes.GrowingPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ObservableEntitiesToObjectsMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.ReactiveObjectsMapper;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.tile.Tab;
import one.modality.base.client.tile.TabsBar;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.event.frontoffice.activities.booking.views.CenterDisplayView;
import one.modality.event.frontoffice.activities.booking.views.EventView;
import one.modality.event.frontoffice.activities.booking.views.SearchBarView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class BookingActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {
    private final VBox internationalEventsContainer = new VBox(20);
    private final VBox localEventsContainer = new VBox(20);
    private final TabsBar<Entity> tabsBar = new TabsBar<>(this, this::showLocalEventsOfType);
    private final FlexPane localEventTypeTabsPane = new FlexPane();
    private final ObservableList<Event> localEvents = FXCollections.observableArrayList();
    private final ObservableList<Event> localEventsOfSelectedType = FXCollections.observableArrayList();

    @Override
    public Node buildUi() {
        Label headerLabel = GeneralUtility.getMainHeaderLabel("eventsHeader");
        headerLabel.setTextAlignment(TextAlignment.CENTER);

        String headerImageUrl = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.event.frontoffice.activity.booking").getString("headerImageUrl");
        ImageView headerImageView = ImageStore.createImageView(headerImageUrl);
        ScalePane headerImageScalePane = new ScalePane(headerImageView);
        headerImageScalePane.setMaxHeight(300);

        Label internationalEventsLabel = GeneralUtility.createLabel("internationalEvents", Color.web(StyleUtility.VICTOR_BATTLE_BLACK), 16);

        Node centerDisplay = new CenterDisplayView().getView( this, this);

        Label localEventsLabel = GeneralUtility.createLabel("localEvents", Color.web(StyleUtility.VICTOR_BATTLE_BLACK), 16);

        Node searchBar = new SearchBarView().getView();

        VBox.setMargin(headerLabel, new Insets(5, 0, 5, 0));
        VBox.setMargin(internationalEventsLabel, new Insets(20));
        VBox.setMargin(centerDisplay, new Insets(10, 0, 25, 0));
        VBox.setMargin(searchBar, new Insets(25));

        VBox container = new VBox(
                headerLabel,
                headerImageScalePane,
                internationalEventsLabel,
                internationalEventsContainer,
                centerDisplay,
                localEventsLabel,
                searchBar,
                localEventTypeTabsPane,
                new GrowingPane(localEventsContainer));
        container.setAlignment(Pos.CENTER);
        container.setBackground(Background.fill(Color.WHITE));
        container.setMaxWidth(1200);

        FXProperties.runOnPropertiesChange(() -> {
            GeneralUtility.screenChangeListened(container.getWidth());
        }, container.widthProperty());


        localEvents.addListener((InvalidationListener) observable -> {
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
                factory = IndividualEntityToObjectMapper.createFactory(EventView::new, EventView::setEvent, EventView::getView);
        ObservableEntitiesToObjectsMapper<Event, ? extends IndividualEntityToObjectMapper<Event, Node>>
                entitiesToObjectsMapper = new ObservableEntitiesToObjectsMapper<>(localEventsOfSelectedType, factory, (Event e, IndividualEntityToObjectMapper<Event, Node> m) -> m.onEntityChangedOrReplaced(e), (Event e1, IndividualEntityToObjectMapper<Event, Node> m1) -> m1.onEntityRemoved(e1));
        ObservableLists.bindConverted(localEventsContainer.getChildren(), entitiesToObjectsMapper.getMappedObjects(), IndividualEntityToObjectMapper::getMappedObject);

        return ControlUtil.createVerticalScrollPane(new BorderPane(container));
    }

    private void showLocalEventsOfType(Entity eventType) {
        localEventsOfSelectedType.setAll(localEvents.filtered(e -> e.getForeignEntity("type") == eventType));
    }

    protected void startLogic() {
        // Loading NKT Festivals
        ReactiveObjectsMapper.<Event, Node>createPushReactiveChain(this)
                .always("{class: 'Event', alias: 'e', fields:'name, label.<loadAll>, live, openingDate, startDate, endDate, organization, organization.country, venue.(name, label.<loadAll>, country), host, frontend, image.url, shortDescriptionLabel.<loadAll>', where: 'organization.type.code = `CORP` and endDate > now()', orderBy: 'startDate, id', limit: 3}")
                .setIndividualEntityToObjectMapperFactory(IndividualEntityToObjectMapper.createFactory(EventView::new, EventView::setEvent, EventView::getView))
                .storeMappedObjectsInto(internationalEventsContainer.getChildren())
                .start();

        // Loading local events
        ReactiveObjectsMapper.<Event, Node>createPushReactiveChain(this)
                .always("{class: 'Event', fields:'name, label.<loadAll>, live, openingDate, startDate, endDate, organization, organization.country, venue.(name, label.<loadAll>, country), host, frontend, image.url, shortDescriptionLabel.<loadAll>, type.(name,label.<loadAll>,ord)', where: 'endDate > now() and type.name != `Not event`', orderBy: 'startDate'}")
                .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), orgId -> where("organization=?", orgId))
                .storeEntitiesInto(localEvents)
                .start();
    }
}
