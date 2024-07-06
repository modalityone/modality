package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.platform.util.tuples.Pair;
import dev.webfx.stack.i18n.I18n;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecurringEventSchedule {
    private final FlexPane ecompassingFlexPane = new FlexPane();
    private final ObservableList<ScheduledItem> scheduledItemsList = FXCollections.observableArrayList();
    protected ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList();
    Map<LocalDate, Pair<ScheduledItemToPane, Pair<String,String>>> paneAndCssClassMap = new HashMap<>();

    private Function<LocalDate, String> computeCssClassForSelectedDateFunction = localDate -> getSelectedDateCssClass();

    private Function<LocalDate, String> computeCssClassForUnselectedDateFunction = localDate -> getUnselectedDateCssClass();

    private Function<LocalDate, Node> computeNodeForExistingBookedDateFunction = localDate -> getDefaultNodeForExistingBookedDate();


    protected Consumer<LocalDate> dateConsumer = null;

    //We define the property on the css and the default value
    private final ObjectProperty<String> selectedDateCssClassProperty = new SimpleObjectProperty<>( "date-selected");
    private final ObjectProperty<String> unselectedDateCssClassProperty = new SimpleObjectProperty<>( "date-unselected");
    private final ObjectProperty<String> existingBookedDateNodeProperty = new SimpleObjectProperty<>( null);

    private ListChangeListener<LocalDate> onChangeDateListener;



    public RecurringEventSchedule() {
        ecompassingFlexPane.setVerticalSpace(30);
        ecompassingFlexPane.setHorizontalSpace(30);
        ecompassingFlexPane.setFlexLastRow(false);

        scheduledItemsList.addListener((InvalidationListener) observable -> {
            Platform.runLater(() -> {
                ecompassingFlexPane.getChildren().clear();
                dev.webfx.platform.util.collection.Collections.forEach(scheduledItemsList, scheduledItem -> {
                    ScheduledItemToPane currentPane = new ScheduledItemToPane(scheduledItem);
                    ecompassingFlexPane.getChildren().add(currentPane.getContainerVBox());
                });
            });
        });

        onChangeDateListener = change -> {LocalDate date = null;
            while (change.next()) {
                if (change.wasAdded()) {
                    date = change.getAddedSubList().get(0);
                }
                if (change.wasRemoved()) {
                    //We remove from the updateStore and the ScheduledItem
                    date = change.getRemoved().get(0);
                }
            }
            changeCssPropertyForSelectedDate(date);
        };
        selectedDates.addListener( onChangeDateListener);
    }
    public void setScheduledItems(List<ScheduledItem> siList) {
        Collections.sort(siList, Comparator.comparing(ScheduledItem::getDate));
        scheduledItemsList.setAll(siList);
    }

    public ScheduledItem getScheduledItem(LocalDate date) {
        return scheduledItemsList.stream()
                .filter(item -> item.getDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    public void addScheduledItemToSelectedList(ScheduledItem s) {
        getSelectedScheduledItem().add(s);
    }

    public void removeScheduledItemFromSelectedList(ScheduledItem s) {
        getSelectedScheduledItem().remove(s);
    }


    public List<ScheduledItem> getScheduledItems()
    {
        return scheduledItemsList;
    }
    public FlexPane buildUi() {
        return ecompassingFlexPane;
    }
    /**
     * This method can be used if we want to customize the behaviour when clicking on a date
     * @param consumer the consumer
     */
    protected void setOnDateClicked(Consumer<LocalDate> consumer)
    {
        dateConsumer = consumer;
    }
    public void setUnselectedDateCssGetter(Function<LocalDate,String> function)
    {
        computeCssClassForUnselectedDateFunction = function;
    }

    public void setComputeNodeForExistingBookedDateFunction(Function<LocalDate, Node> function)
    {
        computeNodeForExistingBookedDateFunction = function;
    }

    public void changeCssPropertyForSelectedDate(LocalDate date) {
        if(date!=null) {
            if (this.selectedDates.contains(date)) {
                changeBackgroundWhenSelected(date, true);
            } else {
                changeBackgroundWhenSelected(date, false);
            }
        }
    }

    public void processDateSelected(LocalDate date)
    {
        if(this.selectedDates.contains(date)) {
            removeSelectedDate(date);
        }
        else {
            addSelectedDate(date);
        }
        Collections.sort(selectedDates);
    }
    protected void addSelectedDate(LocalDate date) {
        this.selectedDates.add(date);
    }

    public void selectAllDates() {
        selectedDates.clear();
        scheduledItemsList.forEach(si->
        {
            selectedDates.add(si.getDate());
        });
    }
    public void selectDates(List <LocalDate> list) {
        selectedDates.clear();
        scheduledItemsList.forEach(si-> {
            if(list.contains(si.getDate())) {
                selectedDates.add(si.getDate());
            }
        });
    }
    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    protected void removeSelectedDate(LocalDate date) {
        this.selectedDates.remove(date);
    }
    protected void changeBackgroundWhenSelected(LocalDate currentDate,boolean isSelected) {
        Pair<ScheduledItemToPane, Pair<String,String>> objectColor = paneAndCssClassMap.get(currentDate);
        ScheduledItemToPane pane = objectColor.get1();
        String cssClassWhenSelected = objectColor.get2().get1();
        String cssClassWhenUnSelected = objectColor.get2().get2();

        if(isSelected) {
            pane.getContainerVBox().getStyleClass().remove(cssClassWhenUnSelected);
            pane.getContainerVBox().getStyleClass().add(cssClassWhenSelected);
        }
        else {
            pane.getContainerVBox().getStyleClass().remove(cssClassWhenSelected);
            pane.getContainerVBox().getStyleClass().add(cssClassWhenUnSelected);
        }
    }

    public ObservableList<ScheduledItem> getSelectedScheduledItem()
    {
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

    public Node getDefaultNodeForExistingBookedDate() {
        return new Label(existingBookedDateNodeProperty.get());
    }

    public Color getCommentColorForExistingBookedDate() {
        return Color.RED;
    }

    private class ScheduledItemToPane {
        Text dayText = new Text();
        HBox dayAndCommentHBox = new HBox(dayText);
        Text hourText = new Text();
        VBox containerVBox = new VBox(dayAndCommentHBox,hourText);
        final int boxWidth = 100;

        private ScheduledItemToPane(ScheduledItem scheduledItem){
            containerVBox.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            containerVBox.setMaxWidth(boxWidth);
            containerVBox.setMinWidth(boxWidth);
            containerVBox.setSpacing(5);
            containerVBox.setPadding(new Insets(5, 0, 5, 0)); // 5px en haut et en bas
            hourText.getStyleClass().add("eventTime");
            containerVBox.setAlignment(Pos.CENTER);
            dayAndCommentHBox.setSpacing(10);
            dayAndCommentHBox.setAlignment(Pos.CENTER);
            LocalDate date = scheduledItem.getDate();
            String dateFormatted = I18n.getI18nText("DateFormatted", I18n.getI18nText(date.getMonth().name()), date.getDayOfMonth());
            dayText.setText(dateFormatted);
            Node comment = computeNodeForExistingBookedDateFunction.apply(date);
            if (comment != null) {
                dayAndCommentHBox.getChildren().add(comment);
            }
            LocalTime startTime = scheduledItem.getStartTime();
            if (startTime == null) {
                startTime = scheduledItem.getTimeline().getStartTime();
            }
            hourText.setText(I18n.getI18nText("AtTime", startTime.toString()));
            paneAndCssClassMap.put(date, new Pair<>(this, new Pair<>(computeCssClassForSelectedDateFunction.apply(date), computeCssClassForUnselectedDateFunction.apply(date))));
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

        public Text getDayText() {
            return dayText;
        }
        public Text getHourText() {
            return hourText;
        }
    }
}
