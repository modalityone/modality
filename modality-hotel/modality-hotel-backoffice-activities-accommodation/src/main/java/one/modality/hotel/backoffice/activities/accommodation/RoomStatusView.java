package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class RoomStatusView {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy");
    private static final FontDef TOP_ROW_FONT = FontDef.font(FontWeight.BOLD, 15);

    private final AccommodationPresentationModel pm;
    private final ObservableList<ScheduledResource> scheduledResources = FXCollections.observableArrayList();
    private final ObservableList<Rate> rates = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> fromProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> fromProperty() { return fromProperty;}

    private final ObjectProperty<LocalDate> toProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> toProperty() { return toProperty; }

    private ObservableValue<Boolean> activeProperty;
    private ReactiveEntitiesMapper<ScheduledResource> rem;
    private ReactiveEntitiesMapper<Rate> ratesRem;
    private final ObjectProperty<String> commaSeparatedRoomIds = new SimpleObjectProperty<>();

    public RoomStatusView(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public Node buildView() {
        VBox body = new VBox();
        scheduledResources.addListener((ListChangeListener<? super ScheduledResource>) change -> {
            //body.getChildren().setAll(createRows());
            String commaSeparatedRoomIdsString = scheduledResources.stream()
                    .map(scheduledResource -> scheduledResource.getResourceConfiguration().getItem().getPrimaryKey())
                    .map(String::valueOf)
                    .distinct()
                    .collect(Collectors.joining(","));
            commaSeparatedRoomIds.set(commaSeparatedRoomIdsString);
        });
        rates.addListener((ListChangeListener<? super Rate>) change -> {
            int size = rates.size();
            System.out.println(size);
        });
        HBox topRow = createTopRow();
        BorderPane borderPane = new BorderPane(new ScrollPane(body));
        borderPane.setTop(topRow);
        return borderPane;
    }

    private HBox createTopRow() {
        Label topLeftLabel = new Label("Room status - period view");

        String fromDateString = DATE_FORMATTER.format(fromProperty.get());
        String toDateString = DATE_FORMATTER.format(toProperty.get());
        Label topRightLabel = new Label("From " + fromDateString + " to " + toDateString);
        TextTheme.createSecondaryTextFacet(topLeftLabel)
                .requestedFont(TOP_ROW_FONT)
                .style();
        TextTheme.createSecondaryTextFacet(topRightLabel)
                .requestedFont(TOP_ROW_FONT)
                .style();
        Region topRowPadding = new Region();
        HBox.setHgrow(topRowPadding, Priority.ALWAYS);
        HBox topRow = new HBox(topLeftLabel, topRowPadding, topRightLabel);
        topRightLabel.setAlignment(Pos.TOP_RIGHT);
        LuminanceTheme.createTopPanelFacet(topRow)
                .style();
        return topRow;
    }

    private List<Node> createRows() {
        List<Node> rows = new ArrayList<>();
        List<Item> roomTypes = listRoomTypes();
        for (Item roomType : roomTypes) {
            rows.add(buildItemPane(roomType));
        }
        return rows;
    }

    private List<Item> listRoomTypes() {
        return scheduledResources.stream()
                .map(scheduledResource -> scheduledResource.getResourceConfiguration().getItem())
                .distinct()
                //.sorted((item1, item2) -> Integer.valueOf(item1.getOrd()).compareTo(item2.getOrd()))
                .collect(Collectors.toList());
    }

    private Pane buildItemPane(Item item) {
        Label itemLabel = new Label(item.getName());
        TextTheme.createPrimaryTextFacet(itemLabel)
                .requestedFont(TOP_ROW_FONT)
                .style();
        VBox itemPane = new VBox(itemLabel);
        List<ResourceConfiguration> resourceConfigurationsForItem = getResourcesForItem(item, scheduledResources);
        List<String> roomNames = resourceConfigurationsForItem.stream()
                .map(ResourceConfiguration::getName)
                .distinct()
                .collect(Collectors.toList());
        for (String roomName : roomNames) {
            itemPane.getChildren().add(new Label(roomName));

            List<ResourceConfiguration> itemsForRoom = resourceConfigurationsForItem.stream()
                    .filter(rc -> rc.getName().equals(roomName))
                    .collect(Collectors.toList());

            for (ResourceConfiguration itemForRoom : itemsForRoom) {
                itemPane.getChildren().add(buildRow(itemForRoom));
            }
        }
        return itemPane;
    }

    private List<ResourceConfiguration> getResourcesForItem(Item item, List<ScheduledResource> scheduledResources) {
        return scheduledResources.stream()
                .map(ScheduledResource::getResourceConfiguration)
                .filter(resourceConfiguration -> resourceConfiguration.getItem().equals(item))
                .collect(Collectors.toList());
    }

    private HBox buildRow(ResourceConfiguration resourceConfiguration) {
        Label nameLabel = new Label(resourceConfiguration.getName());
        Label fromLabel = new Label("From");
        Label fromDateLabel = new Label(resourceConfiguration.getStartDate() != null ? resourceConfiguration.getEndDate().toString() : "Unknown");
        Label toLabel = new Label("To");
        Label toDateLabel = new Label(resourceConfiguration.getEndDate() != null ? resourceConfiguration.getEndDate().toString() : "Unknown");
        HBox hBox = new HBox(nameLabel, fromLabel, fromDateLabel, toLabel, toDateLabel);
        hBox.setSpacing(8);
        return hBox;
    }

    public void startLogic(Object mixin) { // may be called several times with different mixins (due to workaround)
        // Updating the active property with a OR => mixin1.active || mixin2.active || mixin3.active ...
        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        if (rem == null) { // first call
            rem = ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                    .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,available,online,max,configuration.(name,item.name)'}")
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                    // Restricting events to those appearing in the time window
                    .ifNotNullOtherwise(fromProperty, startDate -> where("sr.date >= ?", startDate), where("1 = 0"))
                    .ifNotNullOtherwise(toProperty, endDate -> where("sr.date <= ?", endDate), where("1 = 0"))
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(scheduledResources)
                    // We are now ready to start
                    .start();

            ratesRem = ReactiveEntitiesMapper.<Rate>createPushReactiveChain(mixin)
                    .always("{class: 'Rate', alias: 'r', fields: 'startDate,endDate,item.id'}")
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    //.ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=?", o))
                    // Restricting events to those appearing in the time window
                    .ifNotNullOtherwise(fromProperty, startDate -> where("r.startDate >= ?", startDate), where("1 = 0"))
                    .ifNotNullOtherwise(toProperty, endDate -> where("r.endDate <= ?", endDate), where("1 = 0"))
                    .ifNotNull(commaSeparatedRoomIds, ids -> where("item.id IN (" + ids + ")"))
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(rates)
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) { // subsequent calls
            rem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
            ratesRem.bindActivePropertyTo(activeProperty);
        }
    }

    private static String commaSepratedIntegers(Collection<Integer> ints) {
        return ints.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
