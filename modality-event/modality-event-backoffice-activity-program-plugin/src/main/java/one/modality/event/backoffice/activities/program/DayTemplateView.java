package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.pickers.DatePicker;
import dev.webfx.extras.time.pickers.DatePickerOptions;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class DayTemplateView {

    private final DayTemplateModel dayTemplateModel;

    private final ObservableList<DayTemplateTimelineView> workingDayTemplateTimelineViews = FXCollections.observableArrayList();

    private final DatePicker datePicker = new DatePicker(new DatePickerOptions()
        .setMultipleSelectionAllowed(true)
        .setPastDatesSelectionAllowed(false)
        .setApplyBorderStyle(false)
        .setApplyMaxSize(false)
        .setSortSelectedDates(true)
    );
    private final ObservableList<DayTemplateDateView> workingDayTemplateDateViews = FXCollections.observableArrayList();
    {
        ObservableLists.bindConverted(workingDayTemplateDateViews, datePicker.getSelectedDates(), date -> new DayTemplateDateView(date, datePicker));
    }

    private final BorderPane mainContainer;
    private final TextField templateNameTextField = new TextField();

    DayTemplateView(DayTemplateModel dayTemplateModel) {
        this.dayTemplateModel = dayTemplateModel;
        dayTemplateModel.setSelectedDates(datePicker.getSelectedDates());
        dayTemplateModel.setSyncUiFromModelRunnable(this::syncUiFromModel);
        dayTemplateModel.setInitFormValidationRunnable(this::initFormValidation);
        ObservableLists.bindConverted(workingDayTemplateTimelineViews, dayTemplateModel.getWorkingDayTemplateTimelines(), DayTemplateTimelineView::new);
        mainContainer = buildUi();
        initFormValidation();
        LocalDate eventStartDate = getDayTemplate().getEvent().getStartDate();
        datePicker.setDisplayedYearMonth(YearMonth.of(eventStartDate.getYear(), eventStartDate.getMonth()));
    }

    BorderPane getPanel() {
        return mainContainer;
    }

    public DayTemplate getDayTemplate() {
        return dayTemplateModel.getDayTemplate();
    }

    private void syncUiFromModel() {
        syncTemplateNameUiFromModel();
        syncSelectedDatesUiFromModel();
    }

/*    private void syncModelFromUi() {
        syncTemplateNameModelFromUi();
    }*/

    private void syncTemplateNameUiFromModel() {
        templateNameTextField.setText(getDayTemplate().getName());
    }

    private void syncTemplateNameModelFromUi() {
        getDayTemplate().setName(templateNameTextField.getText());
    }

    private void syncSelectedDatesUiFromModel() {
        if (getDayTemplate().getDates() != null) {
            datePicker.setSelectedDates(DatesToStringConversion.getDateList(getDayTemplate().getDates()));
        }
    }


    private BorderPane buildUi() {
        //****************************  TIMELINES VBOX ******************************************//
        VBox timelinesContainer = new VBox(5);
        timelinesContainer.setFillWidth(true);
        ObservableLists.bindConverted(timelinesContainer.getChildren(), workingDayTemplateTimelineViews, DayTemplateTimelineView::getView);

        //****************************  SELECTED DATES VBOX  ******************************************//
        VBox listOfSelectedDatesVBox = new VBox(10);
        listOfSelectedDatesVBox.setAlignment(Pos.CENTER);
        ObservableLists.bindConverted(listOfSelectedDatesVBox.getChildren(), workingDayTemplateDateViews, DayTemplateDateView::getView);

        //****************************  TOP  ******************************************//
        Label duplicateButton = I18nControls.bindI18nProperties(new Label(), ProgramI18nKeys.DuplicateIcon);
        duplicateButton.setOnMouseClicked(e -> dayTemplateModel.duplicate());
        duplicateButton.setCursor(Cursor.HAND);

        //templateNameTextField.setMinWidth(350);
        Separator topSeparator = new Separator();
        topSeparator.setPadding(new Insets(10, 0, 10, 0));

        templateNameTextField.setPromptText("Name this template"); // ???
        HBox.setHgrow(templateNameTextField, Priority.ALWAYS);
        syncTemplateNameUiFromModel();
        FXProperties.runOnPropertiesChange(this::syncTemplateNameModelFromUi, templateNameTextField.textProperty());

        HBox topLine = new HBox(20, templateNameTextField, duplicateButton);

        Line verticalLine = new Line();
        verticalLine.setStartY(0);
        verticalLine.setEndY(180);
        verticalLine.setStroke(Color.LIGHTGRAY);

        //****************************  CENTER  ******************************************//
        BorderPane centerBorderPane = new BorderPane();
        centerBorderPane.setTop(timelinesContainer);

        SVGPath addIcon = SvgIcons.setSVGPathFill(SvgIcons.createPlusPath(), Color.web("#0096D6"));
        MonoPane addButton = SvgIcons.createButtonPane(addIcon, dayTemplateModel::addTemplateTimeline);
        addButton.setPadding(new Insets(10, 0, 0, 0));

        centerBorderPane.setCenter(addButton);
        BorderPane.setAlignment(addButton, Pos.TOP_LEFT);

        Label deleteButton = Bootstrap.small(Bootstrap.textDanger(I18nControls.bindI18nProperties(new Label(), ProgramI18nKeys.DeleteDayTemplate)));
        SvgIcons.armButton(deleteButton, dayTemplateModel::deleteDayTemplate);
        deleteButton.setPadding(new Insets(30, 0, 0, 0));

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        VBox bottomVBox = new VBox(deleteButton, separator);
        centerBorderPane.setBottom(bottomVBox);
        BorderPane.setAlignment(bottomVBox, Pos.BOTTOM_LEFT);
        centerBorderPane.setMaxHeight(Region.USE_PREF_SIZE);

        //****************************  BOTTOM  ******************************************//
        BorderPane bottomBorderPane = new BorderPane();
        BorderPane.setAlignment(bottomBorderPane, Pos.CENTER);
        bottomBorderPane.setMaxWidth(600);
        Label assignDateLabel = I18nControls.bindI18nProperties(new Label(), ProgramI18nKeys.AssignDay);
        TextTheme.createPrimaryTextFacet(assignDateLabel).style();
        assignDateLabel.getStyleClass().add(Bootstrap.SMALL);
        assignDateLabel.setPadding(new Insets(5, 0, 10, 0));

        ScrollPane listOfSelectedDatesVBoxScrollPane = new ScrollPane(listOfSelectedDatesVBox);
        listOfSelectedDatesVBoxScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        listOfSelectedDatesVBoxScrollPane.setMaxHeight(150);
        listOfSelectedDatesVBoxScrollPane.setMinWidth(120);

        bottomBorderPane.setTop(assignDateLabel);
        BorderPane.setAlignment(assignDateLabel, Pos.CENTER);
        bottomBorderPane.setCenter(datePicker.getView());
        BorderPane.setAlignment(datePicker.getView(), Pos.CENTER);
        Separator verticalSeparator = new Separator(Orientation.VERTICAL);
        verticalSeparator.setPadding(new Insets(0, 0, 0, 40));
        HBox listOfDatesHBox = new HBox(verticalSeparator, listOfSelectedDatesVBoxScrollPane);
        listOfDatesHBox.setSpacing(40);
        listOfDatesHBox.setAlignment(Pos.CENTER);
        bottomBorderPane.setRight(listOfDatesHBox);
        BorderPane.setAlignment(listOfDatesHBox, Pos.CENTER);

        //We define the behaviour when we add or remove a date
        datePicker.getSelectedDates().addListener((ListChangeListener<LocalDate>) change -> {
            DayTemplate dayTemplate = getDayTemplate();
            while (change.next()) {
                if (change.wasAdded()) {
                    // Handle added dates
                    for (LocalDate date : change.getAddedSubList()) {
                        dayTemplate.setDates(DatesToStringConversion.addDate(dayTemplate.getDates(), date));
                    }
                }
                if (change.wasRemoved()) {
                    // Handle removed dates
                    for (LocalDate date : change.getRemoved()) {
                        dayTemplate.setDates(DatesToStringConversion.removeDate(dayTemplate.getDates(), date));
                    }
                }
            }
        });

        BorderPane mainContainer = new BorderPane();
        mainContainer.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
            BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
        mainContainer.setPadding(new Insets(10, 10, 10, 10));
        mainContainer.setTop(new VBox(topLine, topSeparator));
        mainContainer.setCenter(centerBorderPane);
        mainContainer.setMaxHeight(Region.USE_PREF_SIZE);
        mainContainer.setBottom(bottomBorderPane);

        return mainContainer;
    }

    void initFormValidation() {
        dayTemplateModel.getValidationSupport().addRequiredInput(templateNameTextField);
    }

}
