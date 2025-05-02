package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
public class DaySwitcher {

    private static final double DAY_BUTTON_WIDTH = 150;

    private final MonoPane parentContainer;

    private List<LocalDate> dateList;
    private final ObjectProperty<LocalDate> currentDateProperty = new SimpleObjectProperty<>();
    private BorderPane mobileViewContainer;
    private final VBox desktopViewContainer = new VBox(30);
    private final ColumnsPane dayButtonsColumnsPane = new ColumnsPane();
    private final String title;

    private final HashMap<LocalDate, Button> correspondenceDateButton = new HashMap<>();
    private final Button selectAllDaysButton = Bootstrap.button(I18nControls.newButton(VideosI18nKeys.ViewAllDays));

    private final Label mobileDateLabel = Bootstrap.h4(new Label());;
    private final Pane backArrowPane = SvgIcons.createBackArrow2();
    private final Pane forwardArrowPane = SvgIcons.createForwardArrow2();

    public DaySwitcher(List<LocalDate> availableDates, LocalDate initialSelectedDate, MonoPane container, String title) {
        parentContainer = container;
        this.title = title;
        dateList = availableDates.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        setDay(initialSelectedDate);

        buildDesktopUI();
        buildMobileUI();

        // Dynamically update label and arrow visibility when the date changes
        FXProperties.runNowAndOnPropertyChange(this::updateDaysButtonStyle, currentDateProperty);
    }

    private void buildDesktopUI() {
        desktopViewContainer.setAlignment(Pos.TOP_CENTER);
        if (title != null) {
            Label titleLabel = Bootstrap.h3(I18nControls.newLabel(title));
            desktopViewContainer.getChildren().add(titleLabel);
        }
        Label selectDayLabel = I18nControls.newLabel(VideosI18nKeys.SelectTheDayBelow);
        desktopViewContainer.getChildren().addAll(selectDayLabel, dayButtonsColumnsPane);
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
        backArrowPane.setOnMouseClicked(e -> {
            int index = dateList.indexOf(currentDateProperty.get());
            currentDateProperty.set(index == -1 ? Collections.last(dateList) : Collections.get(dateList, index - 1));
        });

        forwardArrowPane.setOnMouseClicked(e -> {
            int index = dateList.indexOf(currentDateProperty.get());
            currentDateProperty.set(Collections.get(dateList, index + 1));
        });

        mobileViewContainer = new BorderPane();
        mobileViewContainer.setLeft(backArrowPane);
        mobileViewContainer.setCenter(mobileDateLabel);
        mobileViewContainer.setRight(forwardArrowPane);
        mobileViewContainer.getStyleClass().add("program-box");
        mobileViewContainer.setPadding(new Insets(10));
        Insets arrowMargin = new Insets(0, 5, 0, 5);
        BorderPane.setMargin(backArrowPane, arrowMargin);
        BorderPane.setMargin(forwardArrowPane, arrowMargin);
    }

    public ObjectProperty<LocalDate> currentDateProperty() {
        return currentDateProperty;
    }

    public Node getMobileViewContainer() {
        return mobileViewContainer;
    }

    public Node getDesktopView() {
        return desktopViewContainer;
    }

    public void populateDates(List<LocalDate> dates) {
        dateList = dates;
        refreshButtonsOnDateListChanges();
    }

    private void refreshButtonsOnDateListChanges() {
        correspondenceDateButton.clear();
        correspondenceDateButton.put(null, selectAllDaysButton);
        dayButtonsColumnsPane.getChildren().clear();
        dateList.forEach((LocalDate day) -> {
            Button dateButton;
            dateButton = Bootstrap.button(new Button());
            dateButton.textProperty().bind(LocalizedTime.formatLocalDateProperty(day, FrontOfficeTimeFormats.DAY_MONTH_DATE));
            dateButton.setMinWidth(DAY_BUTTON_WIDTH);
            correspondenceDateButton.put(day, dateButton);
            dateButton.setOnAction(e -> currentDateProperty.set(day));
            dayButtonsColumnsPane.getChildren().add(dateButton);
        });

        dayButtonsColumnsPane.getChildren().add(selectAllDaysButton);

        // 2nd case: the current Date is not in the list, we select all buttons
        //Here we resize daysColumnPane
        int numberOfChild = dayButtonsColumnsPane.getChildren().size();
        int theoreticalColumnPaneWidth = (int) ((DAY_BUTTON_WIDTH + dayButtonsColumnsPane.getHgap()) * (numberOfChild));
        DoubleProperty theoreticalWidthProperty = new SimpleDoubleProperty(theoreticalColumnPaneWidth);
        dayButtonsColumnsPane.maxWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.min(parentContainer.getWidth(), theoreticalWidthProperty.get()),
                parentContainer.widthProperty(),
                theoreticalWidthProperty
            )
        );

        updateDaysButtonStyle();
    }

    private void updateDaysButtonStyle() {
        LocalDate selectedDate = currentDateProperty.get();
        for (Map.Entry<LocalDate, Button> entry : correspondenceDateButton.entrySet()) {
            Button button = entry.getValue();
            LocalDate buttonDate = entry.getKey();
            boolean isAllDaysButton = buttonDate == null;
            boolean highlighted = isAllDaysButton ? selectedDate == null : // condition for the all-days button
                buttonDate.equals(selectedDate); // condition for a specific-day button
            Collections.addIfNotContainsOrRemove(button.getStyleClass(),  highlighted, Bootstrap.BTN_PRIMARY);
            Collections.addIfNotContainsOrRemove(button.getStyleClass(), !highlighted, ModalityStyle.BTN_WHITE);
        }
        if (selectedDate == null) {
            I18nControls.bindI18nProperties(mobileDateLabel, VideosI18nKeys.ViewAllDays);
        } else {
            mobileDateLabel.textProperty().bind(LocalizedTime.formatLocalDateProperty(selectedDate, FrontOfficeTimeFormats.DAY_MONTH_DATE));
        }
    }

    public void setDay(LocalDate date) {
        if (dateList.contains(date)) {
            currentDateProperty.set(date);
        } else {
            currentDateProperty.set(null);
        }
    }

}
