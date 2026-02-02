package one.modality.crm.backoffice.operations.entities.mail;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import javafx.scene.layout.Pane;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;

public final class ComposeNewMailRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<ComposeNewMailRequest, Void> {

    private final static String OPERATION_CODE = "ComposeNewMail";

    private final Document document;
    private final Pane parentContainer;

    public ComposeNewMailRequest(Document document) {
        this(document, FXMainFrameDialogArea.getDialogArea());
    }

    public ComposeNewMailRequest(Document document, Pane parentContainer) {
        this.document = document;
        this.parentContainer = parentContainer;
    }

    Document getDocument() {
        return document;
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
        return I18nKeys.appendEllipsis(MailI18nKeys.ComposeNewMail);
    }

    @Override
    public AsyncFunction<ComposeNewMailRequest, Void> getOperationExecutor() {
        return ComposeNewMailExecutor::executeRequest;
    }
}
