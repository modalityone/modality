package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import one.modality.base.client.time.BackOfficeTimeFormats;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.markers.EntityHasItem;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class RoomStatusView {

    private static final FontDef TOP_ROW_FONT = FontDef.font(FontWeight.BOLD, 15);
    private static final FontDef RATE_FONT = FontDef.font(FontWeight.NORMAL, 12);

    private final AccommodationPresentationModel pm;
    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final ObservableList<Rate> rates = FXCollections.observableArrayList();
    private final ObjectProperty<LocalDate> fromProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> fromProperty() { return fromProperty;}
    private final DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter(BackOfficeTimeFormats.ROOM_STATUS_DATE_FORMAT);

    private final ObjectProperty<LocalDate> toProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> toProperty() { return toProperty; }

    private ObservableValue<Boolean> activeProperty;
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
            VBox roomsPane = new VBox();
            for (ResourceConfiguration rc : listRoomsOfType(roomType)) {
                roomsPane.getChildren().add(createRoomPane(rc));
            }
            CollapsiblePane rooms = new CollapsiblePane(roomType.getName(), 0, roomsPane);
            roomTypePanes.getChildren().add(rooms);
        }
        return roomTypePanes;
    }

    private List<ResourceConfiguration> listRoomsOfType(Item roomType) {
        return resourceConfigurationLoader.getResourceConfigurations().stream()
                .filter(rc -> rc.getItem().equals(roomType))
                .collect(Collectors.toList());
    }

    private VBox createRoomPane(ResourceConfiguration rc) {
        return new CollapsiblePane(rc.getName(), 1, createRateHistoryGridPane(rc));
    }

    private static class CollapsiblePane extends VBox {

        private static final String PREFIX_EXPAND = "(+)";
        private static final String PREFIX_COLLAPSE = "(-)";
        private static final FontDef ROOM_NAME_FONT = FontDef.font(FontWeight.BOLD, 15);

        private final String headingText;
        private final Label headingLabel = new Label();
        private final Node body;

        public CollapsiblePane(String headingText, int indentationLevel, Node body) {
            this.headingText = headingText;
            this.body = body;
            headingLabel.setOnMouseClicked(e -> expandOrCollapse());
            headingLabel.setPadding(new Insets(0, 0, 0, 32 * indentationLevel));
            expandOrCollapse();
        }

        public void expandOrCollapse() {
            boolean wasExpanded = !headingLabel.getText().startsWith(PREFIX_COLLAPSE);
            String newHeadingText = (wasExpanded ? PREFIX_COLLAPSE : PREFIX_EXPAND) + headingText;
            Platform.runLater(() -> {
                headingLabel.setText(newHeadingText);
                if (wasExpanded) {
                    getChildren().setAll(headingLabel);
                } else {
                    getChildren().setAll(headingLabel, body);
                }
            });
            updateLabelStyle(headingLabel, !wasExpanded);
        }

        private void updateLabelStyle(Label label, boolean selected) {
            if (selected) {
                TextTheme.createPrimaryTextFacet(label)
                        .requestedFont(ROOM_NAME_FONT)
                        .style();
            } else {
                TextTheme.createSecondaryTextFacet(label)
                        .requestedFont(ROOM_NAME_FONT)
                        .style();
            }
        }
    }

    private GridPane createRateHistoryGridPane(ResourceConfiguration rc) {
        // TODO find all rates between the start and end date for the resource item
        List<Rate> ratesForRoom = rates.stream()
                .filter(rate -> rate.getItem().equals(rc.getItem()))
                .sorted(Comparator.comparing(Rate::getStartDate))
                .collect(Collectors.toList());
        GridPane gridPane = new GridPane();
        gridPane.setHgap(16);
        int rowIndex = 0;
        for (Rate rate : ratesForRoom) {
            gridPane.add(createRateLabel("From"), 0, rowIndex);
            gridPane.add(createRateLabel(formatDate(rate.getStartDate())), 1, rowIndex);
            gridPane.add(createRateLabel("To"), 2, rowIndex);
            gridPane.add(createRateLabel(formatDate(rate.getEndDate())), 3, rowIndex);
            gridPane.add(createRateLabel("Price"), 4, rowIndex);
            gridPane.add(createRateLabel(priceToString(rate.getPrice()) + " / night"), 5, rowIndex);
            rowIndex++;
        }
        return gridPane;
    }

    private Label createRateLabel(String text) {
        Label label = new Label(text);
        TextTheme.createDefaultTextFacet(label)
                .requestedFont(RATE_FONT)
                .style();
        return label;
    }

    private String formatDate(LocalDate date) {
        return dateFormatter.format(date);
    }

    private static String priceToString(int price) {
        return PriceFormatter.formatWithoutCurrency(price);
    }

    private HBox createTopRow() {
        Label topLeftLabel = new Label("Room status - period view");

        String fromDateString = dateFormatter.format(fromProperty.get());
        String toDateString = dateFormatter.format(toProperty.get());
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
        topRow.setSpacing(32);
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
        if (ratesRem == null) { // first call
            ratesRem = ReactiveEntitiesMapper.<Rate>createPushReactiveChain(mixin)
                    .always( // language=JSON5
                        "{class: 'Rate', alias: 'r', fields: 'startDate,endDate,item.id,price'}")
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=$1", o))
                    // Restricting events to those appearing in the time window
                    .ifNotNullOtherwise(fromProperty, startDate -> where("r.startDate >= $1", startDate), where("1 = 0"))
                    .ifNotNullOtherwise(toProperty, endDate -> where("r.endDate <= $1", endDate), where("1 = 0"))
                    .ifNotNullOtherwise(commaSeparatedRoomIds, ids -> where("item.id in (" + ids + ")"), where("1 = 0"))
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(rates)
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) { // subsequent calls
            ratesRem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
        }
    }

}
