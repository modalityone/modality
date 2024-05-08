package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.panes.FlexPane;
import dev.webfx.platform.console.Console;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecurringEventSchedule {
    private FlexPane ecompassingFlexPane = new FlexPane();
    private ObservableList<ScheduledItem> scheduledItemsList = FXCollections.observableArrayList();
    protected ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList();
    Map<LocalDate, Pair<ScheduledItemToPane, Pair<String,String>>> paneAndCssClassMap = new HashMap<>();

    private Function<LocalDate, String> computeCssClassForSelectedDateFunction = localDate -> getSelectedDateCssClass();

    private Function<LocalDate, String> computeCssClassForUnselectedDateFunction = localDate -> getUnselectedDateCssClass();

    protected Consumer<LocalDate> dateConsumer = null;

    //We define the property on the css and the default value
    private final ObjectProperty<String> selectedDateCssClassProperty = new SimpleObjectProperty<>( "date-selected");
    private final ObjectProperty<String> unselectedDateCssClassProperty = new SimpleObjectProperty<>( "date-unselected");
    private ListChangeListener<LocalDate> onChangeDateListener;



    public RecurringEventSchedule() {
        ecompassingFlexPane.setVerticalSpace(30);
        ecompassingFlexPane.setHorizontalSpace(30);

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
                    //Here we haven't found it in the scheduledItemsReadFromDatabase, so we create it.

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
        scheduledItemsList.clear();
        siList.forEach(currentSi->scheduledItemsList.add(currentSi));
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
        if(this.selectedDates.contains(date))
        {
            removeSelectedDate(date);
        }
        else
        {
            addSelectedDate(date);
        }
        Collections.sort(selectedDates);
    }
    protected void addSelectedDate(LocalDate date)
    {
        this.selectedDates.add(date);
//        changeBackgroundWhenSelected(date,true);
    }

    public void selectAllDates() {
        selectedDates.clear();
        scheduledItemsList.forEach(si->
        {
            selectedDates.add(si.getDate());
          //  changeBackgroundWhenSelected(si.getDate(),true);
        });
    }
    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    protected void removeSelectedDate(LocalDate date) {
        this.selectedDates.remove(date);
        changeBackgroundWhenSelected(date,false);
    }
    protected void changeBackgroundWhenSelected(LocalDate currentDate,boolean isSelected) {
        Pair<ScheduledItemToPane, Pair<String,String>> objectColor = paneAndCssClassMap.get(currentDate);
        ScheduledItemToPane pane = objectColor.get1();
        String cssClassWhenSelected = objectColor.get2().get1();
        String cssClassWhenUnSelected = objectColor.get2().get2();

        if(isSelected) {
            pane.getContainerVBox().getStyleClass().removeAll(cssClassWhenUnSelected);
            pane.getContainerVBox().getStyleClass().add(cssClassWhenSelected);
        }
        else {
            pane.getContainerVBox().getStyleClass().removeAll(cssClassWhenSelected);
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

    private class ScheduledItemToPane {
        Text dayText = new Text();
        Text hourText = new Text();
        VBox containerVBox = new VBox(dayText,hourText);
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
            scheduledItem.onExpressionLoaded("date, startTime").onFailure(Console::log)
                    .onSuccess(x -> Platform.runLater(()-> {
                        LocalDate date = scheduledItem.getDate();
                        String dateFormatted = I18n.getI18nText("DateFormatted",I18n.getI18nText(date.getMonth().name()),date.getDayOfMonth());
                        dayText.setText(dateFormatted);
                        //We test if the StartTime of the scheduledItem is defined. If it's null, we need to look at the info in the timeline associated to the event
                        LocalTime startTime = scheduledItem.getStartTime();
                        if(startTime==null) {
                            startTime = scheduledItem.getTimeline().getStartTime();
                            }
                        hourText.setText(I18n.getI18nText("AtTime",startTime.toString()));
                        paneAndCssClassMap.put(date,new Pair<>(this, new Pair<>(computeCssClassForSelectedDateFunction.apply(date),computeCssClassForUnselectedDateFunction.apply(date))));
                        containerVBox.setOnMouseClicked(event -> {
                            if (dateConsumer == null) {
                                processDateSelected(date);
                            } else {
                                dateConsumer.accept(date);
                            }
                        });
                    }));
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
