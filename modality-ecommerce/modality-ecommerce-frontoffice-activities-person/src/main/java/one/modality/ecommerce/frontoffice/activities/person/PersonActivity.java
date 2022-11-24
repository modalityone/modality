package one.modality.ecommerce.frontoffice.activities.person;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.stack.ui.util.layout.LayoutUtil;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import one.modality.crm.client.activities.login.LoginPanel;
import one.modality.crm.client.controls.personaldetails.PersonalDetailsPanel;
import one.modality.ecommerce.client.activity.bookingprocess.BookingProcessActivity;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.frontoffice.operations.summary.RouteToSummaryRequest;

/**
 * @author Bruno Salmon
 */
final class PersonActivity extends BookingProcessActivity {

    private PersonalDetailsPanel personalDetailsPanel;

    @Override
    protected void createViewNodes() {
        super.createViewNodes();
/*
        BorderPane accountTopNote = new BorderPane();
        Text accountTopText = newText("AccountTopNote");
        accountTopText.setFill(Color.web("#8a6d3b"));
        TextFlow textFlow = new TextFlow(accountTopText);
        textFlow.maxWidthProperty().bind(
                // borderPane.widthProperty().subtract(100) // not yet emulated for GWT
                Properties.compute(pageContainer.widthProperty(), width -> Numbers.toDouble(width.doubleValue() - 100))
        );
        accountTopNote.setLeft(textFlow);
        Button closeButton = newButton(newActionBuilder(REMOVE_ACTION_KEY).removeText().setActionHandler(() -> verticalStack.getChildren().remove(accountTopNote)));
        closeButton.setBorder(BorderUtil.transparentBorder());
        closeButton.setBackground(BackgroundUtil.TRANSPARENT_BACKGROUND);
        accountTopNote.setRight(closeButton);
        accountTopNote.setBackground(BackgroundUtil.newVerticalLinearGradientBackground("rgba(244, 217, 132, 0.8)", "rgba(235, 192, 120, 0.8)", 5));
        accountTopNote.setPadding(new Insets(10));
        accountTopNote.setBorder(BorderUtil.newWebColorBorder("#ebc078", 5));
*/
        ToggleGroup accountToggleGroup = new ToggleGroup();
        FlowPane accountTabs = new FlowPane(new Button(null, newRadioButton("IDontHaveAnAccount", accountToggleGroup)), new Button(null, newRadioButton("IAlreadyHaveAnAccount", accountToggleGroup)));
        ObservableBooleanValue loggedInProperty = FXLoggedIn.loggedInProperty();
        ObservableBooleanValue notLoggedIn = BooleanExpression.booleanExpression(loggedInProperty).not();
        LoginPanel loginPanel = new LoginPanel();
        personalDetailsPanel = new PersonalDetailsPanel(getEvent(), this, pageContainer);
        Node[] tabContents = {new VBox(10, personalDetailsPanel.getSectionPanel(), nextButton), loginPanel.getNode() };
        BorderPane accountPane = new BorderPane();
        accountToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            Node displayedNode = tabContents[accountToggleGroup.getToggles().indexOf(newValue)];
            accountPane.setCenter(displayedNode);
            if (displayedNode == loginPanel.getNode())
                loginPanel.prepareShowing();
        } );
        accountToggleGroup.selectToggle(accountToggleGroup.getToggles().get(0));
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (loggedInProperty.getValue())
                Platform.runLater(() -> accountToggleGroup.selectToggle(accountToggleGroup.getToggles().get(0)));
        }, loggedInProperty);
        verticalStack.getChildren().setAll(
                //LayoutUtil.setUnmanagedWhenInvisible(accountTopNote, notLoggedIn),
                new VBox(LayoutUtil.setUnmanagedWhenInvisible(accountTabs, notLoggedIn)
                        , accountPane)
                );
        syncUiFromModel();
    }

    private void syncUiFromModel() {
        WorkingDocument workingDocument = getEventActiveWorkingDocument();
        if (workingDocument != null)
            personalDetailsPanel.syncUiFromModel(workingDocument.getDocument());
    }

    private void syncModelFromUi() {
        WorkingDocument workingDocument = getEventActiveWorkingDocument();
        if (workingDocument != null)
            personalDetailsPanel.syncModelFromUi(workingDocument.getDocument());
    }

    @Override
    protected void onNextButtonPressed(ActionEvent event) {
        if (personalDetailsPanel.isValid())
            new RouteToSummaryRequest(getEventId(), getHistory()).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncUiFromModel();
    }

    @Override
    public void onPause() {
        super.onPause();
        syncModelFromUi();
        // Clearing the computed price cache on leaving this page in case a personal detail affecting the price (such as age) has been changed
        WorkingDocument workingDocument = getEventActiveWorkingDocument();
        if (workingDocument != null)
            workingDocument.clearComputedPrice();
    }
}
