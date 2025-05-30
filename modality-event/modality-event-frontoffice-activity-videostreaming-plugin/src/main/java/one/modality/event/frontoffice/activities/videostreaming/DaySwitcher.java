package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public class DaySwitcher {

    private static final double DAY_BUTTON_MIN_WIDTH = 150;

    private final MonoPane parentContainer;

    private List<LocalDate> availableDates;
    private final ObjectProperty<LocalDate> selectedDateProperty = FXProperties.newObjectProperty(this::syncDateButtonsFromSelectedDate);
    private final Object titleI18nKey;

    // Desktop view
    private final VBox desktopViewContainer = new VBox(30);
    private final ColumnsPane dayButtonsColumnsPane = new ColumnsPane();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final HashMap<LocalDate, ToggleButton> dateButtonMap = new HashMap<>();
    private final ToggleButton selectAllDaysButton = Bootstrap.button(I18nControls.newToggleButton(VideoStreamingI18nKeys.ViewAllDays));

    // Mobile view
    private final VBox mobileViewContainer = new VBox(30);
    private final Label mobileDateLabel = Bootstrap.h4(new Label());;
    private final Pane backArrowPane = SvgIcons.createBackArrow2();
    private final Pane forwardArrowPane = SvgIcons.createForwardArrow2();

    public DaySwitcher(List<LocalDate> availableDates, LocalDate initialSelectedDate, MonoPane container, Object titleI18nKey) {
        parentContainer = container;
        this.titleI18nKey = titleI18nKey;
        this.availableDates = availableDates.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        setSelectedDate(initialSelectedDate);

        selectAllDaysButton.setToggleGroup(toggleGroup);
        // Date buttons behave like standard toggle buttons, except that we can't unselect them by clicking on the same
        // button again. So if the user does that, we undo that deselection and reestablish the selection.
        FXProperties.runOnPropertyChange(selectedToggle -> {
            if (selectedToggle == null) // indicates that the user clicked on the selected date button again
                syncDateButtonsFromSelectedDate(); // we reestablish the button selection from the selected date
        }, toggleGroup.selectedToggleProperty());

        buildDesktopView();
        buildMobileView();
    }

    private void buildDesktopView() {
        dayButtonsColumnsPane.setHgap(7);
        dayButtonsColumnsPane.setVgap(15);
        //dayButtonsColumnsPane.setMaxColumnCount(8);
        dayButtonsColumnsPane.setMinColumnWidth(DAY_BUTTON_MIN_WIDTH);
        dayButtonsColumnsPane.setPadding(new Insets(0, 0, 30, 0));

        selectAllDaysButton.setMinWidth(DAY_BUTTON_MIN_WIDTH);
        selectAllDaysButton.setOnAction(e -> selectedDateProperty.set(null));

        if (titleI18nKey != null) {
            Label titleLabel = Bootstrap.h3(I18nControls.newLabel(titleI18nKey));
            desktopViewContainer.getChildren().add(titleLabel);
        }
        desktopViewContainer.getChildren().addAll(
            TimeZoneSwitch.createTimezoneSwitchBox(),
            I18nControls.newLabel(VideoStreamingI18nKeys.SelectTheDayBelow),
            dayButtonsColumnsPane);
        desktopViewContainer.setAlignment(Pos.TOP_CENTER);
    }

    private void buildMobileView() {
        backArrowPane.setOnMouseClicked(e -> {
            int index = availableDates.indexOf(selectedDateProperty.get());
            selectedDateProperty.set(index == -1 ? Collections.last(availableDates) : Collections.get(availableDates, index - 1));
        });

        forwardArrowPane.setOnMouseClicked(e -> {
            int index = availableDates.indexOf(selectedDateProperty.get());
            selectedDateProperty.set(Collections.get(availableDates, index + 1));
        });

        BorderPane daySelectorPane = new BorderPane();
        daySelectorPane.setLeft(backArrowPane);
        daySelectorPane.setCenter(mobileDateLabel);
        daySelectorPane.setRight(forwardArrowPane);
        daySelectorPane.getStyleClass().add("program-box");
        daySelectorPane.setPadding(new Insets(10));
        Insets arrowMargin = new Insets(0, 5, 0, 5);
        BorderPane.setMargin(backArrowPane, arrowMargin);
        BorderPane.setMargin(forwardArrowPane, arrowMargin);

        Hyperlink selectAllDaysLink = I18nControls.newHyperlink(VideoStreamingI18nKeys.ViewAllDays);
        selectAllDaysLink.setOnAction(e -> selectedDateProperty.set(null));

        mobileViewContainer.getChildren().setAll(
            daySelectorPane,
            selectAllDaysLink,
            TimeZoneSwitch.createTimezoneSwitchBox()
        );
        mobileViewContainer.setAlignment(Pos.CENTER);
    }

    public ObjectProperty<LocalDate> selectedDateProperty() {
        return selectedDateProperty;
    }

    public Node getMobileViewContainer() {
        return mobileViewContainer;
    }

    public Node getDesktopView() {
        return desktopViewContainer;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
        // Automatically selecting today if today is part of the event
        LocalDate today = LocalDate.now();
        if (selectedDateProperty.get() == null && availableDates.contains(today)) {
            selectedDateProperty.set(today);
        }

        dateButtonMap.clear();
        dayButtonsColumnsPane.getChildren().clear();
        availableDates.forEach((LocalDate day) -> {
            ToggleButton dateButton = formatLabeledDate(Bootstrap.button(new ToggleButton()), day);
            dateButton.setToggleGroup(toggleGroup);
            dateButton.setMinWidth(DAY_BUTTON_MIN_WIDTH);
            dateButton.setOnAction(e -> selectedDateProperty.set(day));
            dateButtonMap.put(day, dateButton);
            dayButtonsColumnsPane.getChildren().add(dateButton);
        });

        dateButtonMap.put(null, selectAllDaysButton);
        dayButtonsColumnsPane.getChildren().add(selectAllDaysButton);

        // 2nd case: the current Date is not in the list, we select all buttons
        // Here we resize daysColumnPane
        int numberOfChild = dayButtonsColumnsPane.getChildren().size();
        double theoreticalColumnPaneWidth = (DAY_BUTTON_MIN_WIDTH + dayButtonsColumnsPane.getHgap()) * numberOfChild;
        dayButtonsColumnsPane.maxWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.min(parentContainer.getWidth(), theoreticalColumnPaneWidth),
                parentContainer.widthProperty()
            )
        );

        syncDateButtonsFromSelectedDate();
    }

    private void syncDateButtonsFromSelectedDate() {
        LocalDate selectedDate = selectedDateProperty.get();
        // Updating the selected toggle and the date label (on mobile)
        if (selectedDate == null) {
            toggleGroup.selectToggle(selectAllDaysButton);
            I18nControls.bindI18nProperties(mobileDateLabel, VideoStreamingI18nKeys.AllDays);
        } else {
            toggleGroup.selectToggle(dateButtonMap.get(selectedDate));
            formatLabeledDate(mobileDateLabel, selectedDate);
        }
        // Marking the selected date button with the CSS class "fired"
        for (Map.Entry<LocalDate, ToggleButton> entry : dateButtonMap.entrySet()) {
            ToggleButton dateButton = entry.getValue();
            LocalDate buttonDate = entry.getKey();
            Collections.addIfNotContainsOrRemove(dateButton.getStyleClass(), Objects.equals(selectedDate, buttonDate), "fired");
        }
    }

    public void setSelectedDate(LocalDate date) {
        selectedDateProperty.set(availableDates.contains(date) ? date : null);
    }

    private <T extends Labeled> T formatLabeledDate(T labeled, LocalDate date) {
        labeled.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, FrontOfficeTimeFormats.VIDEO_MONTH_DAY_FORMAT));
        return labeled;
    }

}
