package one.modality.booking.backoffice.operations.entities.document.registration;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.HasEntity;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Event;
import one.modality.booking.backoffice.operations.entities.document.BookingDocumentI18nKeys;

public final class ShowNewBookingEditorRequest implements HasOperationCode, HasI18nKey, HasEntity,
        HasOperationExecutor<ShowNewBookingEditorRequest, Void> {

    private final static String OPERATION_CODE = "ShowNewBookingEditor";

    private final Event event;
    private final Pane parentContainer;

    public ShowNewBookingEditorRequest(Event event) {
        this(event, FXMainFrameDialogArea.getDialogArea());
    }

    public ShowNewBookingEditorRequest(Event event, Pane parentContainer) {
        this.event = event;
        this.parentContainer = parentContainer;
    }

    Event getEvent() {
        return event;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return BookingDocumentI18nKeys.ShowNewBookingEditor;
    }

    @Override
    public Event getEntity() {
        return event;
    }

    @Override
    public AsyncFunction<ShowNewBookingEditorRequest, Void> getOperationExecutor() {
        return ShowNewBookingEditorExecutor::executeRequest;
    }
}
