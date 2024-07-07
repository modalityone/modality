package one.modality.ecommerce.history.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.SystemUserId;
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
        // We must know the document for the history
        if (document == null) { // may happen, but in that case documentLine is not null, and we can read the document from the database
            return documentLine.onExpressionLoaded("document")
                    .compose(v -> {
                        if (documentLine.getDocument() == null)
                            return Future.failedFuture("Document not found in database");
                        return prepareDocumentHistoryBeforeSubmit(comment, documentLine.getDocument(), documentLine, userId);
                    });
        }

        // Now we are set to insert the History entity
        UpdateStore updateStore = (UpdateStore) document.getStore();
        History history = updateStore.insertEntity(History.class);
        history.setDocument(document);
        history.setComment(comment);

        // Final step in the history creation is to set the user
        return setHistoryUser(history, userId);
    }

    private static Future<History> setHistoryUser(History history, Object userId) {
        // To record who made the changes, we can 1) set userPerson if available, or 2) set username otherwise
        if (userId instanceof ModalityUserPrincipal) { // Case 1) Should be most cases
            ModalityUserPrincipal mup = (ModalityUserPrincipal) userId;
            history.setUserPerson(mup.getUserPersonId());
            return Future.succeededFuture(history);
        }
        if (userId instanceof SystemUserId) {
            history.setUsername(((SystemUserId) userId).getName());
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
            } else if (e instanceof AbstractMoneyTransferEvent) {
                AbstractMoneyTransferEvent amte = (AbstractMoneyTransferEvent) e;
                resolvePrimaryKeyField(MoneyTransfer.class, amte::getMoneyTransferPrimaryKey, amte::setMoneyTransferPrimaryKey, updateStore);
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
        // We need to know the document associated to the payment
        Document document = payment.getDocument();
        // If not found, we may still know its id (this happens when ServerPaymentServiceProvider adds a payment)
        if (document == null) {
            EntityId documentId = payment.getDocumentId();
            if (documentId != null) { // If we know its id, that's enough, and we just create the document in the store
                document = updateStore.createEntity(documentId);
            }
        }
        // If still not found, we need to load it from the database (this happens when ServerPaymentServiceProvider updates a payment)
        if (document == null) {
            return payment.onExpressionLoaded("document,amount") // we also read the amount (necessary for UpdateMoneyTransferEvent serialization)
                    .compose(v -> {
                        if (payment.getDocument() == null)
                            return Future.failedFuture("Payment document not found in database");
                        return preparePaymentHistoryBeforeSubmit(comment, payment, userId);
                    });
        }

        // Now we are set to insert the History entity
        History history = updateStore.insertEntity(History.class);
        history.setForeignField("moneyTransfer", payment);
        history.setDocument(document);
        history.setComment(comment);

        // Final step in the history creation is to set the user
        return setHistoryUser(history, userId);
    }

}