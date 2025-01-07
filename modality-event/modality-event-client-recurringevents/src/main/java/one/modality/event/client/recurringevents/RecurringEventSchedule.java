package one.modality.event.client.recurringevents;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.util.collection.HashList;
import dev.webfx.stack.i18n.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecurringEventSchedule implements BookableDatesUi {

    private final ColumnsPane columnsPane = new ColumnsPane();
    private final ObservableList<ScheduledItem> scheduledItemsList = FXCollections.observableArrayList();
    protected ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList(); // indicates the dates to add to the current booking
    private final List<LocalDate> clickedDates = new HashList<>(); // Same as selected dates, or larger as it keeps clicked dates when changing the person to book
    private final Map<LocalDate, ScheduledItemBox> scheduledItemBoxes = new HashMap<>();

    private final Function<LocalDate, String> computeCssClassForSelectedDateFunction = localDate -> getSelectedDateCssClass();
    private Function<LocalDate, String> computeCssClassForUnselectedDateFunction = localDate -> getUnselectedDateCssClass();
    //private Function<LocalDate, Node> computeNodeForExistingBookedDateFunction = localDate -> getDefaultNodeForExistingBookedDate();
    private Consumer<LocalDate> dateClickedHandler = null;

    // We define the property on the css and the default value
    private final ObjectProperty<String> selectedDateCssClassProperty = new SimpleObjectProperty<>( "date-selected");
    private final ObjectProperty<String> unselectedDateCssClassProperty = new SimpleObjectProperty<>( "date-unselected");
    //private final ObjectProperty<String> existingBookedDateNodeProperty = new SimpleObjectProperty<>( );

    private final ObjectProperty<Font> dayFontProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Font> timeFontProperty = new SimpleObjectProperty<>();

    public RecurringEventSchedule() {
        columnsPane.setMaxColumnCount(4);
        columnsPane.setMaxWidth(800);

        FXProperties.runOnDoublePropertyChange(width -> {
            double gap = width / 800 * 20;
            columnsPane.setHgap(gap);
            columnsPane.setVgap(gap);
            columnsPane.setMinColumnWidth(Math.max(100, width / 5));
            columnsPane.setMinRowHeight(columnsPane.getColWidth() * 0.4);
            double fontFactor = columnsPane.getColWidth() / 200;
            dayFontProperty.set(Font.font(20 * fontFactor));
            timeFontProperty.set(Font.font(15 * fontFactor));
        }, columnsPane.widthProperty());

        // We bind the children to scheduled items, mapping each to a ScheduledItemBox
        ObservableLists.bindConverted(columnsPane.getChildren(), scheduledItemsList, si -> new ScheduledItemBox(si).getContainerVBox());

        // We keep the dates styles updated on selection change
        ObservableLists.runOnListChange(change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::changeCssPropertyForSelectedDate);
                change.getRemoved().forEach(this::changeCssPropertyForSelectedDate);
            }
        }, selectedDates);
    }

    @Override
    public void setScheduledItems(List<ScheduledItem> scheduledItems, boolean reapplyClickedDates) {
        scheduledItems.sort(Comparator.comparing(ScheduledItem::getDate));
        scheduledItemsList.setAll(scheduledItems);
        selectedDates.clear();
        List<LocalDate> clickedDatesToReapply = !reapplyClickedDates ? Collections.emptyList() : scheduledItemsList.stream()
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
     * This method can be used if we want to customize the behaviour when clicking on a date
     * @param dateClickedHandler the dateClickedHandler
     */
    public void setOnDateClicked(Consumer<LocalDate> dateClickedHandler) {
        this.dateClickedHandler = dateClickedHandler;
    }

    public void setUnselectedDateCssGetter(Function<LocalDate,String> function) {
        computeCssClassForUnselectedDateFunction = function;
    }

    /*public void setComputeNodeForExistingBookedDateFunction(Function<LocalDate, Node> function) {
        computeNodeForExistingBookedDateFunction = function;
    }*/

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
        scheduledItemsList.stream()
                .map(ScheduledItem::getDate)
                .filter(dates::contains)
                .forEach(selectedDates::add);
    }

    public void addClickedDates(List<LocalDate> dates) {
        scheduledItemsList.stream()
                .map(ScheduledItem::getDate)
                .filter(dates::contains)
                .forEach(this::addClickedDate);
    }

    @Override
    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    protected void changeBackgroundWhenSelected(LocalDate date, boolean isSelected) {
        ScheduledItemBox scheduledItemBox = scheduledItemBoxes.get(date);
        String cssClassWhenSelected = computeCssClassForSelectedDateFunction.apply(date);
        String cssClassWhenUnSelected = computeCssClassForUnselectedDateFunction.apply(date);

        ObservableList<String> styleClass = scheduledItemBox.getContainerVBox().getStyleClass();
        if (isSelected) {
            styleClass.remove(cssClassWhenUnSelected);
            styleClass.add(cssClassWhenSelected);
        } else {
            styleClass.remove(cssClassWhenSelected);
            styleClass.add(cssClassWhenUnSelected);
        }
    }

    @Override
    public ObservableList<ScheduledItem> getSelectedScheduledItem() {
        return FXCollections.observableList(scheduledItemsList.stream()
                .filter(item -> selectedDates.contains(item.getDate()))
                .collect(Collectors.toList()));
    }

    public String getSelectedDateCssClass() {
        return selectedDateCssClassProperty.get();
    }

    public String getUnselectedDateCssClass() {
        return unselectedDateCssClassProperty.get();
    }

    /*public Node getDefaultNodeForExistingBookedDate() {
        return new Label(existingBookedDateNodeProperty.get());
    }*/

    private class ScheduledItemBox {

        private final Text dayText = new Text();
        private final HBox dayAndCommentHBox = new HBox(dayText);
        private final Text hourText = new Text();
        private final VBox containerVBox = new VBox(dayAndCommentHBox, hourText);

        private ScheduledItemBox(ScheduledItem scheduledItem) {
            containerVBox.getStyleClass().add("event-date-cell");
            containerVBox.setAlignment(Pos.CENTER);
            containerVBox.setMinWidth(0);
            containerVBox.setMaxWidth(Double.MAX_VALUE);
            containerVBox.setSpacing(5);
            hourText.getStyleClass().add("event-time");
            hourText.fontProperty().bind(timeFontProperty);
            dayAndCommentHBox.setSpacing(10);
            dayAndCommentHBox.setAlignment(Pos.CENTER);
            LocalDate date = scheduledItem.getDate();
            String dateFormatted = I18n.getI18nText(RecurringEventsI18nKeys.DateFormatted1, I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
            dayText.setText(dateFormatted);
            dayText.fontProperty().bind(dayFontProperty);
            /* Commented for now as it's not used and returns an empty label that however shift the date (not centered anymore)
            Node comment = computeNodeForExistingBookedDateFunction.apply(date);
            if (comment != null) {
                dayAndCommentHBox.getChildren().add(comment);
            }*/
            LocalTime startTime = scheduledItem.getStartTime();
            if (startTime == null) {
                Timeline timeline = scheduledItem.getTimeline();
                if (timeline != null)
                    startTime = timeline.getStartTime();
            }
            if (startTime != null) {
                hourText.setText(I18n.getI18nText(RecurringEventsI18nKeys.AtTime0, startTime.toString()));
            }
            scheduledItemBoxes.put(date, this);
            containerVBox.setOnMouseClicked(event -> {
                if (dateClickedHandler == null) {
                    processClickedDate(date);
                } else {
                    dateClickedHandler.accept(date);
                }
            });
            changeCssPropertyForSelectedDate(date);
        }


        public Pane getContainerVBox(){
            return containerVBox;
        }
    }
}
