package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import one.modality.base.shared.entities.Document;

public final class EditDocumentPersonalDetailsRequest implements HasOperationCode,
        HasOperationExecutor<EditDocumentPersonalDetailsRequest, Void> {

    private final static String OPERATION_CODE = "EditDocumentPersonalDetails";

    private final Document document;

    private final ButtonSelectorParameters buttonSelectorParameters;

    public EditDocumentPersonalDetailsRequest(Document document, ButtonSelectorParameters buttonSelectorParameters) {
        this.document = document;
        this.buttonSelectorParameters = buttonSelectorParameters;
    }

    Document getDocument() {
        return document;
    }

    public ButtonSelectorParameters getButtonSelectorParameters() {
        return buttonSelectorParameters;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<EditDocumentPersonalDetailsRequest, Void> getOperationExecutor() {
        return EditDocumentPersonalDetailsExecutor::executeRequest;
    }
}
