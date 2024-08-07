package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecurringEventSchedule {

    private final ColumnsPane columnsPane = new ColumnsPane();
    private final ObservableList<ScheduledItem> scheduledItemsList = FXCollections.observableArrayList();
    protected ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList();
    private final Map<LocalDate, ScheduledItemBox> scheduledItemBoxes = new HashMap<>();

    private final Function<LocalDate, String> computeCssClassForSelectedDateFunction = localDate -> getSelectedDateCssClass();
    private Function<LocalDate, String> computeCssClassForUnselectedDateFunction = localDate -> getUnselectedDateCssClass();
    //private Function<LocalDate, Node> computeNodeForExistingBookedDateFunction = localDate -> getDefaultNodeForExistingBookedDate();
    private Consumer<LocalDate> dateConsumer = null;

    // We define the property on the css and the default value
    private final ObjectProperty<String> selectedDateCssClassProperty = new SimpleObjectProperty<>( "date-selected");
    private final ObjectProperty<String> unselectedDateCssClassProperty = new SimpleObjectProperty<>( "date-unselected");
    //private final ObjectProperty<String> existingBookedDateNodeProperty = new SimpleObjectProperty<>( );

    private final ObjectProperty<Font> dayFontProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Font> timeFontProperty = new SimpleObjectProperty<>();

    public RecurringEventSchedule() {
        columnsPane.setMaxColumnCount(4);
        columnsPane.setMaxWidth(800);

        FXProperties.runOnPropertiesChange(() -> {
            double width = columnsPane.getWidth();
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
        selectedDates.addListener((ListChangeListener<LocalDate>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(this::changeCssPropertyForSelectedDate);
                change.getRemoved().forEach(this::changeCssPropertyForSelectedDate);
            }
        });
    }

    public void setScheduledItems(List<ScheduledItem> siList) {
        siList.sort(Comparator.comparing(ScheduledItem::getDate));
        scheduledItemsList.setAll(siList);
    }

    public Pane buildUi() {
        return columnsPane;
    }

    /**
     * This method can be used if we want to customize the behaviour when clicking on a date
     * @param consumer the consumer
     */
    public void setOnDateClicked(Consumer<LocalDate> consumer) {
        dateConsumer = consumer;
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

    public void processDateSelected(LocalDate date) {
        if (selectedDates.contains(date)) {
            removeSelectedDate(date);
        } else {
            addSelectedDate(date);
        }
        Collections.sort(selectedDates);
    }

    protected void addSelectedDate(LocalDate date) {
        selectedDates.add(date);
    }

    public void selectDates(List <LocalDate> list) {
        selectedDates.clear();
        scheduledItemsList.forEach(si-> {
            if (list.contains(si.getDate())) {
                selectedDates.add(si.getDate());
            }
        });
    }

    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    protected void removeSelectedDate(LocalDate date) {
        selectedDates.remove(date);
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
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
            dayText.setText(dateFormatted);
            dayText.fontProperty().bind(dayFontProperty);
            /* Commented for now as it's not used and returns an empty label that however shift the date (not centered anymore)
            Node comment = computeNodeForExistingBookedDateFunction.apply(date);
            if (comment != null) {
                dayAndCommentHBox.getChildren().add(comment);
            }*/
            LocalTime startTime = scheduledItem.getStartTime();
            if (startTime == null) {
                startTime = scheduledItem.getTimeline().getStartTime();
            }
            hourText.setText(I18n.getI18nText("AtTime", startTime.toString()));
            scheduledItemBoxes.put(date, this);
            containerVBox.setOnMouseClicked(event -> {
                if (dateConsumer == null) {
                    processDateSelected(date);
                } else {
                    dateConsumer.accept(date);
                }
            });
            changeCssPropertyForSelectedDate(date);
        }


        public Pane getContainerVBox(){
            return containerVBox;
        }
    }
}
