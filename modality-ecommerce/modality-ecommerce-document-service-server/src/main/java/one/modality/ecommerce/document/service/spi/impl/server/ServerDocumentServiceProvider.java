package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.*;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
public class ServerDocumentServiceProvider implements DocumentServiceProvider {

    public final static String POLICY_SCHEDULED_ITEMS_QUERY_BASE = "select site.name,item.(name,code,family.code),date,startTime from ScheduledItem";
    public final static String POLICY_RATES_QUERY_BASE = "select site,item,price,perDay,perPerson from Rate";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[] {
                        new QueryArgumentBuilder()
                                .setStatement(POLICY_SCHEDULED_ITEMS_QUERY_BASE + " where event = ?")
                                .setParameters(argument.getEventPk())
                                .setLanguage("DQL")
                                .setDataSourceId(DataSourceModelService.getDefaultDataSourceId())
                                .build(),
                        new QueryArgumentBuilder()
                                .setStatement(POLICY_RATES_QUERY_BASE + " where site = (select venue from Event where id = ?)")
                                .setParameters(argument.getEventPk())
                                .setLanguage("DQL")
                                .setDataSourceId(DataSourceModelService.getDefaultDataSourceId())
                                .build()
                }))
                .map(batch -> new PolicyAggregate(
                        POLICY_SCHEDULED_ITEMS_QUERY_BASE, batch.get(0),
                        POLICY_RATES_QUERY_BASE, batch.get(1)));
    }

    @Override
    public Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument) {
        EntityStoreQuery query;
        if (argument.getDocumentPrimaryKey() != null) {
            query = new EntityStoreQuery("select changes from History where document=? order by id", new Object[] { argument.getDocumentPrimaryKey() });
        } else {
            query = new EntityStoreQuery("select changes from History where document = (select Document where person=? and event=? and !cancelled order by id desc limit 1) order by id", new Object[] { argument.getPersonPrimaryKey(), argument.getEventPrimaryKey() });
        }
        return EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                .<History>executeQuery(query)
                .map(historyList -> {
                    if (historyList.isEmpty()) {
                        return null;
                    }
                    List<AbstractDocumentEvent> documentEvents = new ArrayList<>();
                    historyList.forEach(history -> {
                        ReadOnlyAstArray astArray = AST.parseArray(history.getChanges(), "json");
                        AbstractDocumentEvent[] events = SerialCodecManager.decodeAstArrayToJavaArray(astArray, AbstractDocumentEvent.class);
                        documentEvents.addAll(Arrays.asList(events));
                    });
                    return new DocumentAggregate(null, documentEvents);
                });
    }

    @Override
    public Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        Document document = null;
        DocumentLine documentLine = null;
        AbstractDocumentEvent[] documentEvents = argument.getDocumentEvents();
        for (AbstractDocumentEvent e : documentEvents) {
            if (e instanceof AddDocumentEvent) {
                AddDocumentEvent ade = (AddDocumentEvent) e;
                document = updateStore.insertEntity(Document.class, ade.getDocumentPrimaryKey());
                document.setEvent(ade.getEventPrimaryKey());
                document.setPerson(ade.getPersonPrimaryKey());
                document.setFieldValue("activity", 12); // GP TODO: remove activity from DB
            } else if (e instanceof AddDocumentLineEvent) {
                AddDocumentLineEvent adle = (AddDocumentLineEvent) e;
                documentLine = updateStore.insertEntity(DocumentLine.class, adle.getDocumentLinePrimaryKey());
                documentLine.setDocument(document = updateStore.getOrCreateEntity(Document.class, adle.getDocumentPrimaryKey()));
                documentLine.setSite(adle.getSitePrimaryKey());
                documentLine.setItem(adle.getItemPrimaryKey());
            } else if (e instanceof AddAttendancesEvent) {
                AddAttendancesEvent aae = (AddAttendancesEvent) e;
                documentLine = updateStore.getOrCreateEntity(DocumentLine.class, aae.getDocumentLinePrimaryKey());
                Object[] attendancesPrimaryKeys = aae.getAttendancesPrimaryKeys();
                Object[] scheduledItemsPrimaryKeys = aae.getScheduledItemsPrimaryKeys();
                for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
                    Attendance attendance = updateStore.insertEntity(Attendance.class, attendancesPrimaryKeys[i]);
                    attendance.setDocumentLine(documentLine);
                    attendance.setScheduledItem(scheduledItemsPrimaryKeys[i]);
                }
            } else if (e instanceof CancelDocumentEvent) {
                document = updateStore.updateEntity(Document.class, e.getDocumentPrimaryKey());
                document.setCancelled(true);
            }
        }
        return prepareHistoryAndSubmitUpdateStore(argument, document, documentLine, updateStore, ThreadLocalStateHolder.getUserId());
    }

    private Future<SubmitDocumentChangesResult> prepareHistoryAndSubmitUpdateStore(SubmitDocumentChangesArgument argument, Document document, DocumentLine documentLine, UpdateStore updateStore, Object userId) {
        if (document == null) {
            return documentLine.onExpressionLoaded("document")
                    .compose(v -> {
                        if (documentLine.getDocument() == null)
                            return Future.failedFuture("Document not found in database");
                        return prepareHistoryAndSubmitUpdateStore(argument, documentLine.getDocument(), documentLine, updateStore, userId);
                    });
        }

        // History recording
        History history = updateStore.insertEntity(History.class);
        history.setDocument(document);
        history.setComment(argument.getHistoryComment());
        // To record who made the changes, we can 1) set userPerson if available, or 2) set username otherwise
        Future<Void> settingUserHistoryFuture; // using a Future because 2) requires an async call to getUserClaims()
        if (userId instanceof ModalityUserPrincipal) { // Should be most cases
            ModalityUserPrincipal mup = (ModalityUserPrincipal) userId;
            history.setUserPerson(mup.getUserPersonId());
            settingUserHistoryFuture = Future.succeededFuture();
        } else { // User not logged in or just through SSO
            settingUserHistoryFuture = AuthenticationService.getUserClaims()
                    .compose(userClaims -> {
                        history.setUsername(userClaims.getUsername());
                        return Future.succeededFuture();
                    } , ex -> {
                        history.setUsername("Online user");
                        return Future.succeededFuture();
                    });
        }

        return settingUserHistoryFuture.compose(ignored ->
                submitUpdateStore(updateStore, document)
                        .onSuccess(result -> {
                            // Completing the history recording by saving the changes (new primary keys can now be resolved)
                            completeHistoryRecording(history, argument.getDocumentEvents(), updateStore);
                        }));
    }

    private Future<SubmitDocumentChangesResult> submitUpdateStore(UpdateStore updateStore, Document document) {
        return updateStore.submitChanges(
                SubmitArgument.builder()
                        .setStatement("select set_transaction_parameters(false)")
                        .setDataSourceId(updateStore.getDataSourceId())
                        .build()
        ).compose(batch -> {
            Object documentPk = document.getPrimaryKey();
            Object documentRef = document.getRef();
            Object cartPk = null;
            String cartUuid = null;
            Cart cart = document.getCart();
            if (cart != null) {
                cartPk = cart.getPrimaryKey();
                cartUuid = cart.getUuid();
            }
            if (cartUuid == null || documentRef == null) {
                return document.onExpressionLoaded("ref,cart.uuid")
                        .map(x -> new SubmitDocumentChangesResult(
                                document.getPrimaryKey(),
                                document.getRef(),
                                document.getCart().getPrimaryKey(),
                                document.getCart().getUuid()));
            }
            return Future.succeededFuture(new SubmitDocumentChangesResult(documentPk, documentRef, cartPk, cartUuid));
        });
    }

    private void completeHistoryRecording(History history, AbstractDocumentEvent[] documentEvents, UpdateStore updateStore) {
        // Completing the history recording by saving the changes (new primary keys can now be resolved)
        History h = updateStore.updateEntity(history); // weird API?
        resolveDocumentEventsPrimaryKeys(documentEvents, updateStore);
        h.setChanges(AST.formatArray(SerialCodecManager.encodeJavaArrayToAstArray(documentEvents), "json"));
        updateStore.submitChanges();
    }

    private void resolveDocumentEventsPrimaryKeys(AbstractDocumentEvent[] documentEvents, UpdateStore updateStore) {
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

    private void resolvePrimaryKeyField(Class<? extends Entity> entityClass, Supplier<Object> getter, Consumer<Object> setter, UpdateStore updateStore) {
        Object primaryKey = getter.get();
        Entity entity = updateStore.getEntity(entityClass, primaryKey);
        if (entity != null) {
            setter.accept(entity.getPrimaryKey());
        }
    }

}
