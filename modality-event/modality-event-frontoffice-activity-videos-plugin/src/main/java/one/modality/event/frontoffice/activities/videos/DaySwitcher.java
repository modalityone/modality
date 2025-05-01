package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DaySwitcher {

    private List<LocalDate> dateList;
    private final ObjectProperty<LocalDate> currentDateProperty = new SimpleObjectProperty<>();
    private HBox mobileViewContainer;
    private final VBox desktopViewContainer = new VBox(30);
    private final ColumnsPane dayButtonsColumnsPane = new ColumnsPane();
    private String title;

    private static final int DAY_BUTTON_WIDTH = 150;
    private final HashMap<LocalDate, Button> correspondenceDateButton = new HashMap<>();
    private final Button selectAllDaysButton = Bootstrap.primaryButton(I18nControls.newButton(VideosI18nKeys.ViewAllDays));

    private Label dateLabel;
    private final MonoPane backArrowMonoPane = new MonoPane();
    private final MonoPane forwardArrowMonoPane = new MonoPane();

    private final MonoPane parentContainer;

    public DaySwitcher(List<LocalDate> availableDates, LocalDate initialSelectedDate, MonoPane container,String title) {
        parentContainer = container;
        this.title = title;
        this.dateList = availableDates.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        setDay(initialSelectedDate);

        buildDesktopUI();
        buildMobileUI();

        // Dynamically update label and arrow visibility
        FXProperties.runNowAndOnPropertiesChange(() -> {
            updateDaysButtonStyle();
            I18nControls.bindI18nProperties(dateLabel,VideosI18nKeys.EventSchedule);
            if(currentDateProperty.get()!=null) {
                I18nControls.bindI18nProperties(dateLabel,LocalizedTime.formatLocalDate(currentDateProperty.get(),FrontOfficeTimeFormats.DAY_MONTH_DATE));
            }
            int currentIndex = dateList.indexOf(currentDateProperty.get());
            backArrowMonoPane.setVisible(currentIndex > 0);
            forwardArrowMonoPane.setVisible(currentIndex < dateList.size() - 1);
        }, currentDateProperty);
    }


    private void buildDesktopUI() {
        desktopViewContainer.setAlignment(Pos.TOP_CENTER);
        if(title!=null) {
            Label titleLabel = Bootstrap.h3(I18nControls.newLabel(title));
            desktopViewContainer.getChildren().add(titleLabel);
        }
        Label selectDayLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
        desktopViewContainer.getChildren().addAll(selectDayLabel,dayButtonsColumnsPane);
        dayButtonsColumnsPane.setHgap(7);
        dayButtonsColumnsPane.setVgap(15);
        dayButtonsColumnsPane.setMaxColumnCount(8);
        dayButtonsColumnsPane.setMinColumnWidth(DAY_BUTTON_WIDTH);
        dayButtonsColumnsPane.setPadding(new Insets(0, 0, 30, 0));

        selectAllDaysButton.setMinWidth(DAY_BUTTON_WIDTH);
        selectAllDaysButton.setOnAction(e -> {
            currentDateProperty.set(null);
        });
    }

    private void buildMobileUI() {
        mobileViewContainer = new HBox(20);
        mobileViewContainer.setAlignment(Pos.CENTER);
        mobileViewContainer.getStyleClass().add("program-box");
        dateLabel = Bootstrap.h4(new Label());
        StackPane backArrow = SvgIcons.createBackArrow2();
        backArrow.setPadding(new Insets(15,0,15,30));
        StackPane forwardArrow = SvgIcons.createBackArrow2();
        forwardArrow.setPadding(new Insets(15,0,15,30));
        backArrowMonoPane.setContent(backArrow);
        forwardArrowMonoPane.setContent(forwardArrow);
        forwardArrowMonoPane.setRotate(180);

        backArrowMonoPane.setOnMouseClicked(e -> {
            int index = dateList.indexOf(currentDateProperty.get());
            if (index > 0) {
                currentDateProperty.set(dateList.get(index - 1));
            }
        });

        forwardArrowMonoPane.setOnMouseClicked(e -> {
            int index = dateList.indexOf(currentDateProperty.get());
            if (index < dateList.size() - 1) {
                currentDateProperty.set(dateList.get(index + 1));
            }
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.SOMETIMES);

        mobileViewContainer.getChildren().addAll(backArrowMonoPane,spacer,dateLabel,spacer2,forwardArrowMonoPane);
    }

    public ObjectProperty<LocalDate> currentDateProperty() {
        return currentDateProperty;
    }

    public Node getMobileViewContainer() {
        return mobileViewContainer;
    }

    public Node getDesktopView() { return desktopViewContainer;}

    public void populateDates(List<LocalDate> dates) {
        dateList = dates;
        refreshButtonsOnDateListChanges();
    }

    private void refreshButtonsOnDateListChanges() {
        correspondenceDateButton.clear();
        correspondenceDateButton.put(null, selectAllDaysButton);
        dayButtonsColumnsPane.getChildren().clear();
        Map<LocalDate, Button> dayButtonMap = new HashMap<>();
        dateList.forEach((LocalDate day) -> {
                Button dateButton;
                dateButton = Bootstrap.primaryButton(new Button(LocalizedTime.formatLocalDate(day,FrontOfficeTimeFormats.DAY_MONTH_DATE)));
                dateButton.setMinWidth(DAY_BUTTON_WIDTH);
                correspondenceDateButton.put(day, dateButton);
                dayButtonMap.put(day, dateButton);
                dateButton.setOnAction(e -> currentDateProperty.set(day));
                dayButtonsColumnsPane.getChildren().add(dateButton);
            });

        dayButtonsColumnsPane.getChildren().add(selectAllDaysButton);

        // 2nd case: the current Date is not in the list, we select all buttons
        //Here we resize daysColumnPane
        int numberOfChild = dayButtonsColumnsPane.getChildren().size();
        int theoricColumnPaneWith = (int) ((DAY_BUTTON_WIDTH + dayButtonsColumnsPane.getHgap()) * (numberOfChild));
        DoubleProperty theoricWidthProperty = new SimpleDoubleProperty(theoricColumnPaneWith);
        dayButtonsColumnsPane.maxWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.min(parentContainer.getWidth(), theoricWidthProperty.get()),
                parentContainer.widthProperty(),
                theoricWidthProperty
            )
        );
    }

    private void updateDaysButtonStyle() {
        LocalDate selectedDate = currentDateProperty.get();
        for (Map.Entry<LocalDate, Button> entry : correspondenceDateButton.entrySet()) {
            Button currentButton = entry.getValue();
            //Platform.runLater(()-> {
            if (selectedDate == null || !dateList.contains(selectedDate)) {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, Bootstrap.BTN_PRIMARY);
            } else if (entry.getKey() != null && entry.getKey().equals(selectedDate)) {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, Bootstrap.BTN_PRIMARY);
            } else {
                currentButton.getStyleClass().setAll("button", Bootstrap.BTN, ModalityStyle.BTN_WHITE);
            }
           // });
        }}

    public void setDay(LocalDate date) {
        if (dateList.contains(date)) {
            currentDateProperty.set(date);
        } else {
            currentDateProperty.set(null);
        }
    }

}
