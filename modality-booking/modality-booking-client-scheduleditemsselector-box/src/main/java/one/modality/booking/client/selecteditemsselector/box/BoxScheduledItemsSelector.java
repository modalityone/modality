package one.modality.booking.client.selecteditemsselector.box;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.util.collection.HashList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.booking.client.scheduleditemsselector.ScheduledItemsSelector;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
public final class BoxScheduledItemsSelector implements ScheduledItemsSelector {

    private final boolean showDayOfWeek;
    private final boolean showTime;

    private final ColumnsPane columnsPane = new ColumnsPane();
    private final ObservableList<ScheduledItem> selectableScheduledItems = FXCollections.observableArrayList();
    private final Map<LocalDate, ScheduledItemBox> scheduledItemBoxes = new HashMap<>();
    private final ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList(); // indicates the dates to add to the current booking
    private final List<LocalDate> clickedDates = new HashList<>(); // Same as selected dates, or larger as it keeps clicked dates when changing the person to book

    private final Function<LocalDate, String> computeCssClassForSelectedDateFunction = localDate -> getSelectedDateCssClass();
    private Function<LocalDate, String> computeCssClassForUnselectedDateFunction     = localDate -> getUnselectedDateCssClass();
    private Consumer<LocalDate> dateClickedHandler = null;

    // We define the property on the CSS and the default value
    private final ObjectProperty<String> selectedDateCssClassProperty   = new SimpleObjectProperty<>( "date-selected");
    private final ObjectProperty<String> unselectedDateCssClassProperty = new SimpleObjectProperty<>( "date-unselected");

    // These font properties are only setting the font size (which depends on the box size)
    private final ObjectProperty<Font> dateFontProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Font> timeFontProperty = new SimpleObjectProperty<>();

    public BoxScheduledItemsSelector(boolean showDayOfWeek, boolean showTime) {
        this.showDayOfWeek = showDayOfWeek;
        this.showTime = showTime;

        columnsPane.setMaxWidth(800);

        FXProperties.runOnDoublePropertyChange(width -> {
            double gap = width / 800 * 20;
            columnsPane.setHgap(gap);
            columnsPane.setVgap(gap);
            columnsPane.setMinColumnWidth(Math.max(50, width / 8));
            columnsPane.setMinRowHeight(columnsPane.getColWidth() * 0.4);
            double fontFactor = columnsPane.getColWidth() / 200;
            dateFontProperty.set(Font.font(20 * fontFactor));
            timeFontProperty.set(Font.font(15 * fontFactor));
        }, columnsPane.widthProperty());

        // We bind the children to scheduled items, mapping each to a ScheduledItemBox
        ObservableLists.bindConverted(columnsPane.getChildren(), selectableScheduledItems, si -> new ScheduledItemBox(si).getBox());

        // We keep the date styles updated on selection change
        ObservableLists.runOnListChange(change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::changeCssPropertyForSelectedDate);
                change.getRemoved().forEach(this::changeCssPropertyForSelectedDate);
            }
        }, selectedDates);
    }

    @Override
    public void setSelectableScheduledItems(List<ScheduledItem> selectableScheduledItems, boolean reapplyClickedDates) {
        // We ensured they are sorted by date
        selectableScheduledItems.sort(Comparator.comparing(ScheduledItem::getDate));
        // We dont show cancelled scheduled items
        this.selectableScheduledItems.setAll(ScheduledItems.filterNotCancelled(selectableScheduledItems));
        selectedDates.clear();
        List<LocalDate> clickedDatesToReapply = !reapplyClickedDates ? Collections.emptyList() : this.selectableScheduledItems.stream()
                .map(ScheduledItem::getDate)
                .filter(clickedDates::contains)
                .collect(Collectors.toList());
        clickedDates.clear();
        addClickedDates(clickedDatesToReapply);
    }

    public Pane buildUi() {
        return columnsPane;
    }

    /**
     * This method can be used if we want to customize the behavior when clicking on a date
     * @param dateClickedHandler the dateClickedHandler
     */
    public void setOnDateClicked(Consumer<LocalDate> dateClickedHandler) {
        this.dateClickedHandler = dateClickedHandler;
    }

    public void setUnselectedDateCssGetter(Function<LocalDate,String> function) {
        computeCssClassForUnselectedDateFunction = function;
    }

    public void changeCssPropertyForSelectedDate(LocalDate date) {
        if (date != null)
            changeBackgroundWhenSelected(date, selectedDates.contains(date));
    }

    public void processClickedDate(LocalDate date) {
        if (selectedDates.contains(date)) {
            removeClickedDate(date);
        } else {
            addClickedDate(date);
        }
        Collections.sort(selectedDates);
    }

    public void addClickedDate(LocalDate date) {
        if (!clickedDates.contains(date))
            clickedDates.add(date);
        if (!selectedDates.contains(date))
            selectedDates.add(date);
    }

    @Override
    public void removeClickedDate(LocalDate date) {
        clickedDates.remove(date);
        selectedDates.remove(date);
    }

    @Override
    public void clearClickedDates() {
        clickedDates.clear();
    }

    @Override
    public void addSelectedDates(List<LocalDate> dates) {
        selectableScheduledItems.stream()
                .map(ScheduledItem::getDate)
                .filter(dates::contains)
                .forEach(selectedDates::add);
    }

    public void addClickedDates(List<LocalDate> dates) {
        selectableScheduledItems.stream()
                .map(ScheduledItem::getDate)
                .filter(dates::contains)
                .forEach(this::addClickedDate);
    }

    @Override
    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    private void changeBackgroundWhenSelected(LocalDate date, boolean isSelected) {
        ScheduledItemBox scheduledItemBox = scheduledItemBoxes.get(date);
        String cssClassWhenSelected = computeCssClassForSelectedDateFunction.apply(date);
        String cssClassWhenUnSelected = computeCssClassForUnselectedDateFunction.apply(date);

        ObservableList<String> styleClass = scheduledItemBox.getBox().getStyleClass();
        if (isSelected) {
            styleClass.remove(cssClassWhenUnSelected);
            styleClass.add(cssClassWhenSelected);
        } else {
            styleClass.remove(cssClassWhenSelected);
            styleClass.add(cssClassWhenUnSelected);
        }
    }

    @Override
    public ObservableList<ScheduledItem> getSelectedScheduledItems() {
        return FXCollections.observableList(selectableScheduledItems.stream()
                .filter(item -> selectedDates.contains(item.getDate()))
                .collect(Collectors.toList()));
    }

    public String getSelectedDateCssClass() {
        return selectedDateCssClassProperty.get();
    }

    public String getUnselectedDateCssClass() {
        return unselectedDateCssClassProperty.get();
    }

    private class ScheduledItemBox {
        private final VBox box = new VBox();

        private ScheduledItemBox(ScheduledItem scheduledItem) {
            box.getStyleClass().add("scheduled-item-box");
            box.setAlignment(Pos.CENTER);
            box.setMinWidth(0);
            box.setMaxWidth(Double.MAX_VALUE);
            box.setSpacing(5);
            LocalDate date = scheduledItem.getDate();
            if (showDayOfWeek) {
                Text dayOfWeekText = new Text();
                dayOfWeekText.getStyleClass().add("day-of-week");
                dayOfWeekText.textProperty().bind(LocalizedTime.formatDayOfWeekProperty(date.getDayOfWeek(), FrontOfficeTimeFormats.BOX_SCHEDULED_ITEM_DAY_OF_WEEK_FORMAT));
                dayOfWeekText.fontProperty().bind(dateFontProperty);
                box.getChildren().add(dayOfWeekText);
            }
            Text dateText = new Text();
            dateText.getStyleClass().add("date");
            dateText.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, FrontOfficeTimeFormats.BOX_SCHEDULED_ITEM_MONTH_DAY_FORMAT));
            dateText.fontProperty().bind(dateFontProperty);
            box.getChildren().add(dateText);
            if (showTime) {
                Text timeText = new Text();
                timeText.getStyleClass().add("time");
                timeText.fontProperty().bind(timeFontProperty);
                LocalTime startTime = scheduledItem.getStartTime();
                if (startTime == null) {
                    Timeline timeline = scheduledItem.getTimeline();
                    if (timeline != null)
                        startTime = timeline.getStartTime();
                }
                if (startTime != null) {
                    I18n.bindI18nProperties(timeText, BoxScheduledItemsSelectorI18nKeys.AtTime1,
                        LocalizedTime.formatLocalTimeProperty(startTime, FrontOfficeTimeFormats.BOX_SCHEDULED_ITEM_TIME_FORMAT));
                }
            }
            scheduledItemBoxes.put(date, this);
            box.setOnMouseClicked(event -> {
                if (dateClickedHandler == null) {
                    processClickedDate(date);
                } else {
                    dateClickedHandler.accept(date);
                }
            });
            changeCssPropertyForSelectedDate(date);
        }

        public Pane getBox(){
            return box;
        }
    }
}
