package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.History;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public final class HistoryRecorder {

    public static Future<History> prepareDocumentHistoryBeforeSubmit(String comment, Document document, DocumentLine documentLine) {
        return prepareDocumentHistoryBeforeSubmit(comment, document, documentLine, ThreadLocalStateHolder.getUserId());
    }

    public static Future<History> prepareDocumentHistoryBeforeSubmit(String comment, Document document, DocumentLine documentLine, Object userId) {
        if (document == null) {
            return documentLine.onExpressionLoaded("document")
                    .compose(v -> {
                        if (documentLine.getDocument() == null)
                            return Future.failedFuture("Document not found in database");
                        return prepareDocumentHistoryBeforeSubmit(comment, documentLine.getDocument(), documentLine, userId);
                    });
        }

        // History recording
        UpdateStore updateStore = (UpdateStore) document.getStore();
        History history = updateStore.insertEntity(History.class);
        history.setDocument(document);
        history.setComment(comment);
        // To record who made the changes, we can 1) set userPerson if available, or 2) set username otherwise
        if (userId instanceof ModalityUserPrincipal) { // Case 1) Should be most cases
            ModalityUserPrincipal mup = (ModalityUserPrincipal) userId;
            history.setUserPerson(mup.getUserPersonId());
            return Future.succeededFuture(history);
        }
        // Case 2) User not logged in or just through SSO
        return AuthenticationService.getUserClaims()
                .compose(userClaims -> {
                    history.setUsername(userClaims.getUsername());
                    return Future.succeededFuture(history);
                } , ex -> {
                    history.setUsername("Online user");
                    return Future.succeededFuture(history);
                });
    }

    public static void completeDocumentHistoryAfterSubmit(History history, AbstractDocumentEvent[] documentEvents) {
        // Completing the history recording by saving the changes (new primary keys can now be resolved)
        UpdateStore updateStore = (UpdateStore) history.getStore();
        History h = updateStore.updateEntity(history); // weird API?
        resolveDocumentEventsPrimaryKeys(documentEvents, updateStore);
        h.setChanges(AST.formatArray(SerialCodecManager.encodeJavaArrayToAstArray(documentEvents), "json"));
        updateStore.submitChanges();
    }

    private static void resolveDocumentEventsPrimaryKeys(AbstractDocumentEvent[] documentEvents, UpdateStore updateStore) {
        for (AbstractDocumentEvent e : documentEvents) {
            resolvePrimaryKeyField(Document.class, e::getDocumentPrimaryKey, e::setDocumentPrimaryKey, updateStore);
            if (e instanceof AbstractDocumentLineEvent) {
                AbstractDocumentLineEvent adle = (AbstractDocumentLineEvent) e;
                resolvePrimaryKeyField(DocumentLine.class, adle::getDocumentLinePrimaryKey, adle::setDocumentLinePrimaryKey, updateStore);
                if (e instanceof AbstractAttendancesEvent) {
                    AbstractAttendancesEvent aae = (AbstractAttendancesEvent) e;
                    Object[] attendancesPrimaryKeys = aae.getAttendancesPrimaryKeys();
                    for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
                        final int fi = i;
                        resolvePrimaryKeyField(Attendance.class, () -> attendancesPrimaryKeys[fi], pk -> attendancesPrimaryKeys[fi] = pk, updateStore);
                    }
                }
            }
        }
    }

    private static void resolvePrimaryKeyField(Class<? extends Entity> entityClass, Supplier<Object> getter, Consumer<Object> setter, UpdateStore updateStore) {
        Object primaryKey = getter.get();
        Entity entity = updateStore.getEntity(entityClass, primaryKey);
        if (entity != null) {
            setter.accept(entity.getPrimaryKey());
        }
    }
}
