package org.modality_project.event.frontoffice.activities.options;

import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.modality_project.base.client.entities.util.Labels;
import org.modality_project.base.client.icons.ModalityIcons;
import org.modality_project.base.shared.entities.Option;
import org.modality_project.ecommerce.client.activity.bookingprocess.BookingProcessActivity;
import org.modality_project.ecommerce.client.businessdata.feesgroup.FeesGroup;
import org.modality_project.ecommerce.client.businessdata.preselection.OptionsPreselection;
import org.modality_project.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import org.modality_project.event.client.controls.bookingcalendar.BookingCalendar;
import org.modality_project.event.client.controls.sectionpanel.SectionPanelFactory;
import org.modality_project.ecommerce.frontoffice.operations.person.RouteToPersonRequest;
import dev.webfx.extras.flexbox.FlexBox;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.webfx.stack.framework.client.ui.util.layout.LayoutUtil.setMaxWidthToInfinite;

/**
 * @author Bruno Salmon
 */
public class OptionsActivity extends BookingProcessActivity {

    private FlexBox topLevelOptionButtonsContainer;
    private Node bookingCalendarSection;
    protected Label priceText;
    private WorkingDocument lastWorkingDocument;

    @Override
    public void onPause() {
        super.onPause();
        lastWorkingDocument = getEventActiveWorkingDocument();
    }

    @Override
    protected void startLogic() {
        boolean forceRefresh = true; //getEventOptions() == null; // forcing refresh in case the working document has changed (ex: going back from the personal details after having changed the age)
        onEventFeesGroups()
                .onFailure(Console::log)
                .onSuccess(feesGroups -> {
                    OptionsPreselection selectedOptionsPreselection = getEventActiveOptionsPreselection();
                    WorkingDocument workingDocument = getEventActiveWorkingDocument();
                    // Detecting if it's a new booking
                    if (workingDocument == null || selectedOptionsPreselection != null && selectedOptionsPreselection.getWorkingDocument() == workingDocument) {
                        // Using no accommodation option by default if no preselection was selected
                        if (selectedOptionsPreselection == null) {
                            selectedOptionsPreselection = findNoAccommodationOptionsPreselection(feesGroups);
                            selectedOptionsPreselection.setEventActive();
                        }
                        // Ensuring the working document is a duplication of the preselection one to not alter the original one
                        selectedOptionsPreselection.createNewWorkingDocument(null).setEventActive(); // And make it active
                    }
                    if (lastWorkingDocument != workingDocument) {
                        if (verticalScrollPane != null)
                            verticalScrollPane.setVvalue(0);
                        optionTree.reset();
                    }
                    createOrUpdateOptionPanelsIfReady(forceRefresh);
                });
    }

    private OptionsPreselection findNoAccommodationOptionsPreselection(FeesGroup[] feesGroups) {
        for (FeesGroup feesGroup : feesGroups) {
            OptionsPreselection noAccommodationPreselection = Arrays.findFirst(feesGroup.getOptionsPreselections(), op -> !op.hasAccommodation());
            if (noAccommodationPreselection != null)
                return noAccommodationPreselection;
        }
        return null;
    }

    @Override
    protected void createViewNodes() {
        super.createViewNodes();

        topLevelOptionButtonsContainer = new FlexBox(4, 4);
        bookingCalendar = createBookingCalendar();
        bookingCalendar.setOnAttendanceChangedRunnable(optionTree::getUpdatedTopLevelOptionSections);
        bookingCalendarSection = SectionPanelFactory.createBookingCalendarSection(bookingCalendar);

        priceText = new Label();
        priceText.textProperty().bind(bookingCalendar.formattedBookingPriceProperty());
        addPriceText();

        createOrUpdateOptionPanelsIfReady(true);
    }

    protected BookingCalendar createBookingCalendar() {
        return new BookingCalendar(true);
    }

    protected BookingCalendar bookingCalendar;
    private final OptionTree optionTree = new OptionTree(this);

    void createOrUpdateOptionPanelsIfReady(boolean forceRefresh) {
        WorkingDocument workingDocument = getEventActiveWorkingDocument();
        if (workingDocument != null && bookingCalendar != null) {
            bookingCalendar.createOrUpdateCalendarGraphicFromWorkingDocument(workingDocument, forceRefresh);

            UiScheduler.runInUiThread(() -> {
                topLevelOptionButtonsContainer.getChildren().setAll(optionTree.getUpdatedTopLevelOptionButtons());
                ObservableList<Node> verticalStackChildren = verticalStack.getChildren();
                verticalStackChildren.setAll(topLevelOptionButtonsContainer);
                verticalStackChildren.addAll(optionTree.getUpdatedTopLevelOptionSections());
                verticalStackChildren.add(bookingCalendarSection);
                verticalStackChildren.add(nextButton);
            });
        }
    }

    protected void addPriceText() {
        priceText.setAlignment(Pos.CENTER);
        pageContainer.setTop(setMaxWidthToInfinite(priceText));
    }

    protected List<Node> createOptionPanelHeaderNodes(Option option, Property<String> i18nTitle) {
        Label label = new Label();
        label.textProperty().bind(i18nTitle);
        return Arrays.asList(ModalityIcons.getItemFamilyIcon16(option), label);
    }

    protected Node createLabelNode(org.modality_project.base.shared.entities.Label label) {
        //HtmlText htmlText = new HtmlText();
        Label htmlText = new Label();
        bindTextWithLabel(htmlText.textProperty(), label);
        return htmlText;
    }

    private final Map<org.modality_project.base.shared.entities.Label, Property<String>> labelTexts = new HashMap<>();

    private void bindTextWithLabel(Property<String> textProperty, org.modality_project.base.shared.entities.Label label) {
        textProperty.bind(Labels.translateLabel(label));
        labelTexts.put(label, textProperty);
    }

    protected void updateLabelText(org.modality_project.base.shared.entities.Label label) {
        Property<String> textProperty = labelTexts.get(label);
        if (textProperty != null)
            bindTextWithLabel(textProperty, label);
    }

    @Override
    protected void onNextButtonPressed(ActionEvent event) {
        if (optionTree.getValidationSupport().isValid())
            new RouteToPersonRequest(getEventId(), getHistory()).execute();
    }
}
