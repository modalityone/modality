package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.ColumnsPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

import java.time.LocalDate;
import java.util.List;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
public class DaySwitcher {

    private static final double DAY_BUTTON_MIN_WIDTH = 150;

    private final MonoPane parentContainer;

    private List<LocalDate> availableDates;
    private final ObjectProperty<LocalDate> selectedDateProperty = FXProperties.newObjectProperty(this::updateMobileDateLabel);
    private final Object titleI18nKey;

    // Desktop view
    private final VBox desktopViewContainer = new VBox(30);
    private final ColumnsPane dayButtonsColumnsPane = new ColumnsPane();
    private final AriaToggleGroup<LocalDate> dayToggleGroup = new AriaToggleGroup<>();

    // Mobile view
    private final VBox mobileViewContainer = new VBox(30);
    private final Label mobileDateLabel = Bootstrap.h4(new Label());;
    private final Pane backArrowPane = SvgIcons.createBackArrow2();
    private final Pane forwardArrowPane = SvgIcons.createForwardArrow2();

    public DaySwitcher(MonoPane container, Object titleI18nKey) {
        parentContainer = container;
        this.titleI18nKey = titleI18nKey;

        buildDesktopView();
        buildMobileView();

        selectedDateProperty.bindBidirectional(dayToggleGroup.firedItemProperty());
    }

    private void buildDesktopView() {
        dayButtonsColumnsPane.setHgap(7);
        dayButtonsColumnsPane.setVgap(15);
        dayButtonsColumnsPane.setMinColumnWidth(DAY_BUTTON_MIN_WIDTH);
        dayButtonsColumnsPane.setPadding(new Insets(0, 0, 30, 0));

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
            int index = availableDates.indexOf(getSelectedDate());
            selectedDateProperty.set(index == -1 ? Collections.last(availableDates) : Collections.get(availableDates, index - 1));
        });

        forwardArrowPane.setOnMouseClicked(e -> {
            int index = availableDates.indexOf(getSelectedDate());
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

        dayToggleGroup.clear();
        dayButtonsColumnsPane.getChildren().clear();
        availableDates.forEach(this::createAndAddDateButton);
        createAndAddDateButton(null); // All-days button

        // Here we resize daysColumnPane
        int numberOfChild = dayButtonsColumnsPane.getChildren().size();
        double theoreticalColumnPaneWidth = (DAY_BUTTON_MIN_WIDTH + dayButtonsColumnsPane.getHgap()) * numberOfChild;
        dayButtonsColumnsPane.maxWidthProperty().bind(
            Bindings.createDoubleBinding(
                () -> Math.min(parentContainer.getWidth(), theoreticalColumnPaneWidth),
                parentContainer.widthProperty()
            )
        );

        // Automatically selecting today if today is part of the event
        if (!availableDates.contains(getSelectedDate()))
            setSelectedDate(LocalDate.now());
        dayToggleGroup.updateButtonsFiredStyleClass();
    }

    private void createAndAddDateButton(LocalDate date) {
        ToggleButton dateButton = formatLabeledDate(Bootstrap.button(dayToggleGroup.createItemButton(date)), date, false);
        dateButton.setMinWidth(DAY_BUTTON_MIN_WIDTH);
        dayButtonsColumnsPane.getChildren().add(dateButton);
    }

    private void updateMobileDateLabel() {
        formatLabeledDate(mobileDateLabel, getSelectedDate(), true);
    }

    public void setSelectedDate(LocalDate date) {
        selectedDateProperty.set(availableDates.contains(date) ? date : null);
    }

    private LocalDate getSelectedDate() {
        return selectedDateProperty.get();
    }

    private <T extends Labeled> T formatLabeledDate(T labeled, LocalDate date, boolean mobile) {
        if (date == null) // All-days
            I18nControls.bindI18nProperties(labeled, mobile ? VideoStreamingI18nKeys.AllDays : VideoStreamingI18nKeys.ViewAllDays);
        else
            labeled.textProperty().bind(LocalizedTime.formatMonthDayProperty(date, FrontOfficeTimeFormats.VIDEO_MONTH_DAY_FORMAT));
        return labeled;
    }

}
