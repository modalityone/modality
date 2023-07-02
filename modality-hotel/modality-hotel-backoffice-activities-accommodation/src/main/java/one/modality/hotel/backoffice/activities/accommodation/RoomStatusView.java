package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.EntityId;
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
import one.modality.base.shared.entities.markers.EntityHasItem;
import one.modality.hotel.backoffice.accommodation.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class RoomStatusView {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yy");
    private static final FontDef TOP_ROW_FONT = FontDef.font(FontWeight.BOLD, 15);

    private final AccommodationPresentationModel pm;
    private final ResourceConfigurationLoader resourceConfigurationLoader;
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
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        resourceConfigurationLoader.getResourceConfigurations().addListener((ListChangeListener<? super ResourceConfiguration>) change -> {
            String roomIds = resourceConfigurationLoader.getResourceConfigurations().stream()
                    .map(rc -> rc.getItem().getId().getPrimaryKey().toString())
                    .distinct()
                    .collect(Collectors.joining(","));
            commaSeparatedRoomIds.set(roomIds);
        });
    }
    public Node buildView() {
        VBox body = new VBox();
        ScrollPane roomTypesScrollPane = new ScrollPane();
        rates.addListener((ListChangeListener<? super Rate>) change -> {
            roomTypesScrollPane.setContent(createRoomTypePanes());
        });
        HBox topRow = createTopRow();
        BorderPane borderPane = new BorderPane(roomTypesScrollPane);
        borderPane.setTop(topRow);
        return borderPane;
    }

    private VBox createRoomTypePanes() {
        List<Item> roomTypes = resourceConfigurationLoader.getResourceConfigurations().stream()
                .map(EntityHasItem::getItem)
                .distinct()
                .collect(Collectors.toList());

        VBox roomTypePanes = new VBox();
        for (Item roomType : roomTypes) {
            Label roomTypeLabel = new Label(roomType.getName());
            VBox roomPane = new VBox(roomTypeLabel);
            for (ResourceConfiguration rc : listRoomsOfType(roomType)) {
                roomPane.getChildren().add(createRoomPane(rc));
            }
            roomTypePanes.getChildren().add(roomPane);
        }
        return roomTypePanes;
    }

    private List<ResourceConfiguration> listRoomsOfType(Item roomType) {
        return resourceConfigurationLoader.getResourceConfigurations().stream()
                .filter(rc -> rc.getItem().equals(roomType))
                .collect(Collectors.toList());
    }

    private VBox createRoomPane(ResourceConfiguration rc) {
        Label roomNameLabel = new Label(rc.getName());
        GridPane historyGridPane = createRateHistoryGridPane(rc);
        VBox roomPane = new VBox(roomNameLabel, historyGridPane);
        return roomPane;
    }

    private GridPane createRateHistoryGridPane(ResourceConfiguration rc) {
        // TODO find all rates between the start and end date for the resource item
        List<Rate> ratesForRoom = rates.stream()
                .filter(rate -> rate.getItem().equals(rc.getItem()))
                .collect(Collectors.toList());
        GridPane gridPane = new GridPane();
        gridPane.setHgap(16);
        int rowIndex = 0;
        for (Rate rate : ratesForRoom) {
            gridPane.add(new Label("From"), 0, rowIndex);
            gridPane.add(new Label(formatDate(rate.getStartDate())), 1, rowIndex);
            gridPane.add(new Label("To"), 2, rowIndex);
            gridPane.add(new Label(formatDate(rate.getEndDate())), 3, rowIndex);
            gridPane.add(new Label("Price"), 4, rowIndex);
            gridPane.add(new Label(priceToString(rate.getPrice()) + " / night"), 5, rowIndex);
            rowIndex++;
        }
        return gridPane;
    }

    private static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    private static String priceToString(int price) {
        if ((price % 100) == 0) {
            return String.valueOf(price / 100);
        } else {
            return new DecimalFormat("#.00").format(price / 100.0);
        }
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
            ratesRem = ReactiveEntitiesMapper.<Rate>createPushReactiveChain(mixin)
                    .always("{class: 'Rate', alias: 'r', fields: 'startDate,endDate,item.id,price'}")
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=?", o))
                    // Restricting events to those appearing in the time window
                    .ifNotNullOtherwise(fromProperty, startDate -> where("r.startDate >= ?", startDate), where("1 = 0"))
                    .ifNotNullOtherwise(toProperty, endDate -> where("r.endDate <= ?", endDate), where("1 = 0"))
                    .ifNotNullOtherwise(commaSeparatedRoomIds, ids -> where("item.id in (" + ids + ")"), where("1 = 0"))
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(rates)
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) { // subsequent calls
            rem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
            ratesRem.bindActivePropertyTo(activeProperty);
        }
    }

}
