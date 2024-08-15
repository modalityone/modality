package one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;

import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.ecommerce.backoffice.operations.entities.document.registration.AbstractSetDocumentFieldsRequest;

public final class MergeMultipleBookingsOptionsRequest extends AbstractSetDocumentFieldsRequest<MergeMultipleBookingsOptionsRequest> {

    private final static String OPERATION_CODE = "MergeMultipleBookingsOptions";

    public MergeMultipleBookingsOptionsRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public MergeMultipleBookingsOptionsRequest(Document document, Pane parentContainer) {
        super(document, parentContainer);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<MergeMultipleBookingsOptionsRequest, Void> getOperationExecutor() {
        return MergeMultipleBookingsOptionsExecutor::executeRequest;
    }
}
