package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.layout.calendar.CalendarLayout;
import dev.webfx.extras.time.layout.node.TimeGridPane;
import dev.webfx.extras.time.layout.node.TimePane;
import dev.webfx.platform.async.impl.Listener;
import dev.webfx.stack.i18n.I18n;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class DatesPicker
{
    private VBox calendarPane;
    private ObjectProperty<YearMonth> monthProperty = new SimpleObjectProperty<>(YearMonth.now()) {
        @Override
        protected void invalidated() {
            super.invalidated();
            refreshForMonth(getMonth());
        }
    };
    private final String ARROW_PREVIOUS_MONTH_SVGPATH = "M14.2187 28C10.6383 28 7.20455 26.525 4.67281 23.8995C2.14107 21.274 0.718751 17.713 0.718751 14C0.718751 10.287 2.14107 6.72601 4.67281 4.1005C7.20455 1.475 10.6383 -7.46609e-07 14.2188 -5.90104e-07C17.7992 -4.33598e-07 21.233 1.475 23.7647 4.10051C26.2964 6.72602 27.7187 10.287 27.7187 14C27.7187 17.713 26.2964 21.274 23.7647 23.8995C21.2329 26.525 17.7992 28 14.2187 28ZM20.125 14.875C20.3488 14.875 20.5634 14.7828 20.7216 14.6187C20.8799 14.4546 20.9687 14.2321 20.9687 14C20.9687 13.7679 20.8799 13.5454 20.7216 13.3813C20.5634 13.2172 20.3488 13.125 20.125 13.125L10.3493 13.125L13.9724 9.3695C14.1308 9.2052 14.2198 8.98236 14.2198 8.75C14.2198 8.51764 14.1308 8.2948 13.9724 8.1305C13.8139 7.9662 13.5991 7.87389 13.375 7.87389C13.1509 7.87389 12.9361 7.9662 12.7776 8.1305L7.71513 13.3805C7.63655 13.4618 7.57421 13.5583 7.53167 13.6646C7.48914 13.7709 7.46724 13.8849 7.46724 14C7.46724 14.1151 7.48914 14.2291 7.53167 14.3354C7.57421 14.4417 7.63655 14.5382 7.71513 14.6195L12.7776 19.8695C12.9361 20.0338 13.1509 20.1261 13.375 20.1261C13.5991 20.1261 13.8139 20.0338 13.9724 19.8695C14.1308 19.7052 14.2198 19.4824 14.2198 19.25C14.2198 19.0176 14.1308 18.7948 13.9724 18.6305L10.3493 14.875L20.125 14.875Z";
    private final String ARROW_NEXT_MONTH_SVGPATH = "M14.7187 1.29539e-06C18.4318 1.13309e-06 21.9927 1.475 24.6182 4.1005C27.2438 6.72601 28.7188 10.287 28.7188 14C28.7188 17.713 27.2438 21.274 24.6182 23.8995C21.9927 26.525 18.4318 28 14.7188 28C11.0057 28 7.44477 26.525 4.81926 23.8995C2.19375 21.274 0.71875 17.713 0.718749 14C0.718749 10.287 2.19375 6.72602 4.81925 4.10051C7.44476 1.475 11.0057 1.45769e-06 14.7187 1.29539e-06ZM8.59375 13.125C8.36169 13.125 8.13913 13.2172 7.97503 13.3813C7.81094 13.5454 7.71875 13.7679 7.71875 14C7.71875 14.2321 7.81094 14.4546 7.97503 14.6187C8.13913 14.7828 8.36169 14.875 8.59375 14.875L18.7315 14.875L14.9743 18.6305C14.8099 18.7948 14.7176 19.0176 14.7176 19.25C14.7176 19.4824 14.8099 19.7052 14.9743 19.8695C15.1386 20.0338 15.3614 20.1261 15.5938 20.1261C15.8261 20.1261 16.0489 20.0338 16.2133 19.8695L21.4633 14.6195C21.5447 14.5382 21.6094 14.4417 21.6535 14.3354C21.6976 14.2291 21.7203 14.1151 21.7203 14C21.7203 13.8849 21.6976 13.7709 21.6535 13.6646C21.6094 13.5583 21.5447 13.4618 21.4633 13.3805L16.2133 8.1305C16.0489 7.9662 15.8261 7.87389 15.5938 7.87389C15.3614 7.8739 15.1386 7.9662 14.9742 8.1305C14.8099 8.2948 14.7176 8.51764 14.7176 8.75C14.7176 8.98236 14.8099 9.2052 14.9742 9.3695L18.7315 13.125L8.59375 13.125Z";

    private ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList();

    private CalendarLayout<YearMonth, YearMonth> yearMonthCalendarLayout;
    private BorderPane yearMonthSelectionHBox;
    private CalendarLayout<DayOfWeek, DayOfWeek> daysOfWeekLayout;
    private TimePane<DayOfWeek, DayOfWeek> daysOfWeekPane;
    private CalendarLayout<LocalDate, LocalDate> daysOfMonthLayout;
    private TimeGridPane<LocalDate, LocalDate> daysOfMonthPane;
    private Label monthYearLabel;
    private SVGPath arrowPreviousMonthPath;
    private SVGPath arrowNextMonthPath;
    public DatesPicker(YearMonth currentMonth)
    {
        setMonth(currentMonth);
        generateCalendarPane(currentMonth);

        //When we changed the selected dates, we reinitialize the display of the calendar
        selectedDates.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                initializeDaysSelected();
            }
        });
    }

    public YearMonth getMonth() {
        return monthProperty.get();
    }

    public ObjectProperty<YearMonth> monthProperty() {
        return monthProperty;
    }

    public void setMonth(YearMonth month) {
        this.monthProperty.set(month);
    }

    private void initialiseForMonth(YearMonth newMonth)
    {
        this.generateCalendarPane(newMonth);
    }

    public void refreshForMonth(YearMonth newMonth)
    {
        //If a property is at null, it means, it's the first call and the object haven't been initialized because the function calling here
        //happens before the initialisation of the objects, so we need to test first if the objects are initialized
        if(yearMonthCalendarLayout!=null) yearMonthCalendarLayout.getChildren().setAll(newMonth);
        if(daysOfMonthLayout!=null)daysOfMonthLayout.getChildren().setAll(TimeUtil.generateMonthDates(newMonth));
        if(arrowPreviousMonthPath!=null)arrowPreviousMonthPath.setOnMouseClicked(event ->  {
            setMonth(newMonth.minusMonths(1));
        });
        if(arrowNextMonthPath!=null)arrowNextMonthPath.setOnMouseClicked(event ->  {
            setMonth(newMonth.plusMonths(1));
        });
        if(monthYearLabel!=null) {
            monthYearLabel.setText(I18n.getI18nText(newMonth.getMonth().name().toUpperCase(Locale.ROOT) + " " + newMonth.getYear()));
            initializeDaysSelected();
        }
    }

    private VBox generateCalendarPane(YearMonth month) {
        calendarPane = new VBox();
        calendarPane.setMaxWidth(300);
        yearMonthCalendarLayout = new CalendarLayout<>();
        yearMonthCalendarLayout.getChildren().setAll(month);
        yearMonthSelectionHBox = new BorderPane();
        yearMonthSelectionHBox.setPadding(new Insets(10, 0, 10, 0));

        arrowPreviousMonthPath = new SVGPath();
        arrowPreviousMonthPath.setContent(ARROW_PREVIOUS_MONTH_SVGPATH);
        arrowPreviousMonthPath.setStrokeWidth(2);
        TextTheme.createPrimaryTextFacet(arrowPreviousMonthPath).style();
        arrowPreviousMonthPath.setScaleX(0.7);
        arrowPreviousMonthPath.setScaleY(0.7);
        arrowPreviousMonthPath.setOnMouseClicked(event ->  {
            setMonth(month.minusMonths(1));
        });

        arrowNextMonthPath = new SVGPath();
        arrowNextMonthPath.setContent(ARROW_NEXT_MONTH_SVGPATH);
        arrowNextMonthPath.setStrokeWidth(2);
        TextTheme.createPrimaryTextFacet(arrowNextMonthPath).style();
        arrowNextMonthPath.setScaleX(0.7);
        arrowNextMonthPath.setScaleY(0.7);
        arrowNextMonthPath.setOnMouseClicked(event ->  {
            setMonth(month.plusMonths(1));
        });

        monthYearLabel = new Label();
        TextTheme.createPrimaryTextFacet(monthYearLabel).setRequestedFont(FontDef.font(FontWeight.SEMI_BOLD, 14)).style();
        monthYearLabel.setText(I18n.getI18nText(month.getMonth().name().toUpperCase(Locale.ROOT) + " " + month.getYear()));
        monthYearLabel.setPadding(new Insets(0, 10, 0, 10));
        yearMonthSelectionHBox.setLeft(arrowPreviousMonthPath);
        yearMonthSelectionHBox.setCenter(monthYearLabel);
        yearMonthSelectionHBox.setRight(arrowNextMonthPath);
        
        daysOfWeekLayout = new CalendarLayout<>();

        daysOfWeekLayout.getChildren().setAll(TimeUtil.generateDaysOfWeek());
        daysOfWeekPane = new TimePane<>(daysOfWeekLayout, this::createDayOfWeekNode);
        //daysOfWeekPane.setBackground(Background.fill(Color.LIGHTBLUE));
        daysOfWeekLayout.setChildFixedHeight(20);

        daysOfMonthLayout = new CalendarLayout<>();
        daysOfMonthLayout.getChildren().setAll(TimeUtil.generateMonthDates(month));

        daysOfMonthPane = new TimeGridPane<>(daysOfMonthLayout, this::createDateNode);
        //daysOfMonthPane.setBackground(Background.fill(Color.LIGHTGRAY));
        //LuminanceTheme.createPrimaryPanelFacet(daysOfMonthPane).style();
        daysOfWeekPane.setPadding(new Insets(10, 0, 20, 0));
        daysOfMonthPane.setHgap(5);
        daysOfMonthPane.setVgap(5);
        calendarPane.getChildren().addAll(yearMonthSelectionHBox, daysOfWeekPane, daysOfMonthPane);
        return calendarPane;
    }

    private Node createDateNode(LocalDate date) {
        Label currentDayLabel = new Label();
        GridPane.setHalignment(currentDayLabel, HPos.CENTER);
        int size = 20;
        currentDayLabel.setMaxSize(size,size);
        currentDayLabel.setMinSize(size,size);
        currentDayLabel.setPrefSize(size,size);
        currentDayLabel.setText(String.valueOf(date.getDayOfMonth()));
        currentDayLabel.setAlignment(Pos.CENTER);
        CornerRadii corner = new CornerRadii(size);
        currentDayLabel.setBackground(Background.EMPTY);
        currentDayLabel.setOnMouseClicked(event ->  {
            this.processDateSelected(currentDayLabel,date);
        });
        // currentDayLabel.getStyleClass().add("hello-border-radius");
        return currentDayLabel;
    }

    private void processDateSelected(Label currentDayLabel,LocalDate date)
    {

        if(this.selectedDates.contains(date))
        {
            this.selectedDates.remove(date);
            changeBackgroundWhenSelected(currentDayLabel,false);
        }
        else
        {
            this.selectedDates.add(date);
            changeBackgroundWhenSelected(currentDayLabel,true);
        }
        Collections.sort(selectedDates);
    }

    private void changeBackgroundWhenSelected(Label currentDayLabel,boolean isSelected)
    {
        CornerRadii corner = new CornerRadii(20);
        Background backgroundWhenSelected = new Background(new BackgroundFill(Color.rgb(175, 231, 255), corner, Insets.EMPTY));
        currentDayLabel.setBackground(Background.EMPTY);
        if(isSelected) {
            currentDayLabel.setBackground(backgroundWhenSelected);
        }
    }

    public void initializeDaysSelected()
    {
        //We take the CalendarLayout Children that are a list of LocalDate element
        //and the TimeGridPane children that are a list of Label, and the i element, of the children of both list are
        //corresponding
        List localDateElements = daysOfMonthLayout.getChildren();
        List labelElements = daysOfMonthPane.getChildren();
        LocalDate currentDate;
        Label currentLabel;
        for (int i = 0; i < localDateElements.size(); i++) {
            currentDate = (LocalDate) localDateElements.get(i);
            currentLabel = (Label) labelElements.get(i);
            changeBackgroundWhenSelected(currentLabel,selectedDates.contains(currentDate));
        }
    }

    public ObservableList<LocalDate> selectedDate()
    {
        return this.selectedDates;
    }

    private Node createDayOfWeekNode(DayOfWeek date) {
        Label currentDayLabel = new Label();
        int size = 20;
        currentDayLabel.setMaxSize(size,size);
        currentDayLabel.setMinSize(size,size);
        currentDayLabel.setPrefSize(size,size);
        //currentDayLabel.setPadding(new Insets(10,0,0,0));

        String dayName =  I18n.getI18nText(date.name().toUpperCase(Locale.ROOT).substring(0,1));
        currentDayLabel.setText(dayName);
        currentDayLabel.setAlignment(Pos.CENTER);
        currentDayLabel.setBackground(Background.EMPTY);
        TextTheme.createSecondaryTextFacet(currentDayLabel).style();
        //currentDayLabel.setPadding(new Insets(20,0,0,0));
        // currentDayLabel.getStyleClass().add("hello-border-radius");
        return currentDayLabel;
    }

    public VBox getCalendarPane() {
        return calendarPane;
    }

    public void setCalendarPane(VBox calendarPane) {
        this.calendarPane = calendarPane;
    }

    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    public void setSelectedDates(ObservableList<LocalDate> selectedDates) {
        this.selectedDates = selectedDates;
    }

}
