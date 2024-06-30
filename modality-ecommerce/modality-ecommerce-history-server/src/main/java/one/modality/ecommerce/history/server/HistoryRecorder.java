package one.modality.ecommerce.history.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.ecommerce.document.service.events.*;

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

        UpdateStore updateStore = (UpdateStore) document.getStore();
        History history = updateStore.insertEntity(History.class);
        history.setDocument(document);
        history.setComment(comment);

        return setHistoryUser(history, userId);
    }

    private static Future<History> setHistoryUser(History history, Object userId) {
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
                }, ex -> {
                    history.setUsername("Online user");
                    return Future.succeededFuture(history);
                });
    }

    public static void completeDocumentHistoryAfterSubmit(History history, AbstractDocumentEvent... documentEvents) {
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
            } else if (e instanceof AddMoneyTransferEvent) {
                AddMoneyTransferEvent ate = (AddMoneyTransferEvent) e;
                resolvePrimaryKeyField(MoneyTransfer.class, ate::getMoneyTransferPrimaryKey, ate::setMoneyTransferPrimaryKey, updateStore);
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

    public static Future<History> preparePaymentHistoryBeforeSubmit(String comment, MoneyTransfer payment) {
        return preparePaymentHistoryBeforeSubmit(comment, payment, ThreadLocalStateHolder.getUserId());
    }

    public static Future<History> preparePaymentHistoryBeforeSubmit(String comment, MoneyTransfer payment, Object userId) {
        UpdateStore updateStore = (UpdateStore) payment.getStore();
        History history = updateStore.insertEntity(History.class);
        history.setForeignField("moneyTransfer", payment);
        Document document = payment.getDocument();
        if (document == null) {
            EntityId documentId = payment.getDocumentId();
            if (documentId != null) {
                document = updateStore.createEntity(documentId);
            }
        }
        history.setDocument(document);
        history.setComment(comment);
        return setHistoryUser(history, userId);
    }

}