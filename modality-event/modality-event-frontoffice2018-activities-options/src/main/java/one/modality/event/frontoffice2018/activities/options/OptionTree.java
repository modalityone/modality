package one.modality.event.frontoffice2018.activities.options;

import javafx.scene.Node;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Option;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client2018.businessdata.workingdocument.WorkingDocumentTransaction;
import one.modality.base.client.util.functions.TranslateFunction;
import one.modality.base.client.validation.ModalityValidationSupport;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class OptionTree {

    private final OptionsActivity activity;
    private Event event;
    private List<Option> topLevelOptions;
    private WorkingDocumentTransaction workingDocumentTransaction;
    private final ModalityValidationSupport validationSupport = new ModalityValidationSupport();

    OptionTree(OptionsActivity activity) {
        this.activity = activity;
        new TranslateFunction<Option>("translateOption") {
            @Override
            protected String translate(Option option) {
                String optionTranslation = bestTranslationOrName(option);
                boolean multiSite = option.getParent() != null && option.getParent().getSite() == null;
                if (multiSite)
                    optionTranslation = bestTranslationOrName(option.getSite()) + " - " + optionTranslation;
                return optionTranslation;
            }
        }.register();
    }

    OptionsActivity getActivity() {
        return activity;
    }

    WorkingDocument getWorkingDocument() {
        return activity.getEventActiveWorkingDocument();
    }

    WorkingDocumentTransaction getWorkingDocumentTransaction() {
        WorkingDocument workingDocument = getWorkingDocument();
        workingDocument.setIsOptionSelectedInOptionTreePredicate(this::isOptionSelected);
        if (workingDocumentTransaction == null || workingDocumentTransaction.getWorkingDocument() != workingDocument)
            workingDocumentTransaction = new WorkingDocumentTransaction(workingDocument);
        return workingDocumentTransaction;
    }

    ModalityValidationSupport getValidationSupport() {
        return validationSupport;
    }

    private void clearDataOnEventChange() {
        Event currentEvent = activity.getEvent();
        if (this.event != currentEvent) {
            topLevelOptions = null;
            this.event = currentEvent;
        }
    }

    private List<Option> getTopLevelOptions() {
        clearDataOnEventChange();
        if (topLevelOptions == null)
            topLevelOptions = Collections.filter(activity.getEventOptions(), Option::hasNoParent);
        return topLevelOptions;
    }

    private List<Option> getTopLevelNonObligatoryOptions() {
        return Collections.filter(getTopLevelOptions(), Option::isNotObligatory);
    }

    List<Node> getUpdatedTopLevelOptionButtons() {
        return Collections.map(getTopLevelNonObligatoryOptions(), this::getUpdatedTopLevelOptionButton);
    }

    List<Node> getUpdatedTopLevelOptionSections() {
        return Collections.map(getTopLevelOptions(), this::getUpdatedTopLevelOptionSection);
    }

    private Node getUpdatedTopLevelOptionButton(Option o) {
        return getOptionTreeNode(o).createOrUpdateTopLevelOptionButtonFromModel();
    }

    private Node getUpdatedTopLevelOptionSection(Option o) {
        return getOptionTreeNode(o).createOrUpdateTopLevelOptionSectionFromModel();
    }

    private final Map<Option, OptionTreeNode> optionTreeNodes = new HashMap<>();

    private OptionTreeNode getOptionTreeNode(Option option) {
        OptionTreeNode optionTreeNode = optionTreeNodes.get(option);
        if (optionTreeNode == null)
            optionTreeNode = new OptionTreeNode(option, this);
        return optionTreeNode;
    }

    void registerOptionTreeNode(OptionTreeNode optionTreeNode) { // Called by OptionTreeNode constructor
        optionTreeNodes.put(optionTreeNode.getOption(), optionTreeNode);
    }

    public boolean isOptionSelected(Option option) {
        OptionTreeNode optionTreeNode = optionTreeNodes.get(option);
        return optionTreeNode != null && (optionTreeNode.isOptionSelectedInModel() || optionTreeNode.isUiOptionSelected(true));
    }

    private boolean pendingTransactionCommitAndUiSync;

    void deferTransactionCommitAndUiSync() {
        if (!pendingTransactionCommitAndUiSync) {
            pendingTransactionCommitAndUiSync = true;
            UiScheduler.scheduleDeferred(() -> {
                getWorkingDocumentTransaction().commit();
                getActivity().createOrUpdateOptionPanelsIfReady(true);
                pendingTransactionCommitAndUiSync = false;
            });
        }
    }

    void reset() {
        Collections.forEach(optionTreeNodes.values(), OptionTreeNode::reset);
    }
}
