package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.i18n.HasI18nKey;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.DocumentI18nKeys;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class ToggleMarkNotMultipleBookingRequest extends AbstractSetDocumentFieldsRequest<ToggleMarkNotMultipleBookingRequest>
    implements HasI18nKey {

    private final static String OPERATION_CODE = "ToggleMarkNotMultipleBooking";

    public ToggleMarkNotMultipleBookingRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ToggleMarkNotMultipleBookingRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return DocumentI18nKeys.ToggleMarkNotMultipleBooking;
    }

    @Override
    public AsyncFunction<ToggleMarkNotMultipleBookingRequest, Void> getOperationExecutor() {
        return ToggleMarkNotMultipleBookingExecutor::executeRequest;
    }
}
