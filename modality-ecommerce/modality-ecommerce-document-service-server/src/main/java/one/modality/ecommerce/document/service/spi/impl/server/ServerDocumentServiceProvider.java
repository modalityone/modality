package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.session.state.ThreadLocalStateHolder;
import one.modality.base.shared.entities.*;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.*;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;

import java.util.HashMap;
import java.util.Map;
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
        return Future.failedFuture("Not yet implemented");
    }

    @Override
    public Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);
        Map<EntityId, Entity> entities = new HashMap<>();
        EntityId documentId = null;
        Document document = null;
        AbstractDocumentEvent[] documentEvents = argument.getDocumentEvents();
        for (AbstractDocumentEvent e : documentEvents) {
            if (e instanceof AddDocumentEvent) {
                AddDocumentEvent ade = (AddDocumentEvent) e;
                document = updateStore.insertEntity(Document.class);
                document.setEvent(ade.getEventPrimaryKey());
                document.setPerson(ade.getPersonPrimaryKey());
                document.setFieldValue("activity", 12); // GP TODO: remove activity from DB
                entities.put(documentId = EntityId.create(Document.class, e.getDocumentPrimaryKey()), document);
            } else if (e instanceof AddDocumentLineEvent) {
                AddDocumentLineEvent adle = (AddDocumentLineEvent) e;
                DocumentLine documentLine = updateStore.insertEntity(DocumentLine.class);
                documentLine.setDocument(entities.get(documentId = EntityId.create(Document.class, adle.getDocumentPrimaryKey())));
                documentLine.setSite(adle.getSitePrimaryKey());
                documentLine.setItem(adle.getItemPrimaryKey());
                entities.put(EntityId.create(DocumentLine.class, adle.getDocumentLinePrimaryKey()), documentLine);
            } else if (e instanceof AddAttendancesEvent) {
                AddAttendancesEvent aae = (AddAttendancesEvent) e;
                Entity documentLine = entities.get(EntityId.create(DocumentLine.class, aae.getDocumentLinePrimaryKey()));
                Object[] attendancesPrimaryKeys = aae.getAttendancesPrimaryKeys();
                Object[] scheduledItemsPrimaryKeys = aae.getScheduledItemsPrimaryKeys();
                for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
                    Attendance attendance = updateStore.insertEntity(Attendance.class);
                    attendance.setDocumentLine(documentLine);
                    attendance.setScheduledItem(scheduledItemsPrimaryKeys[i]);
                    entities.put(EntityId.create(Attendance.class, attendancesPrimaryKeys[i]), attendance);
                }
            } else if (e instanceof CancelDocumentEvent) {
                documentId = EntityId.create(Document.class, e.getDocumentPrimaryKey());
                document = updateStore.updateEntity(Document.class, documentId.getPrimaryKey());
                document.setCancelled(true);
                entities.put(documentId, document);
            }
        }

        if (document != null) { // Should we fail if it is?

            // History recording
            History history = updateStore.insertEntity(History.class);
            history.setDocument(document);
            history.setComment(argument.getHistoryComment());
            // To record who made the changes, we can 1) set userPerson if available, or 2) set username otherwise
            Future<Void> settingUserHistoryFuture; // using a Future because 2) requires an async call to getUserClaims()
            Object userId = ThreadLocalStateHolder.getUserId();
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

            EntityId finalDocumentId = documentId;
            Document finalDocument = document;
            return settingUserHistoryFuture.compose(ignored ->
                    submitUpdateStore(updateStore, finalDocumentId, finalDocument)
                    .map(result -> {
                        // Completing the history recording by saving the changes (new primary keys can now be resolved)
                        History h = updateStore.updateEntity(history); // weird API?
                        resolveDocumentEventsPrimaryKeys(documentEvents, entities);
                        h.setChanges(AST.formatArray(SerialCodecManager.encodeJavaArrayToAstArray(documentEvents), "json"));
                        updateStore.submitChanges();
                        return result;
                    }));

        }

        return submitUpdateStore(updateStore, documentId, document);
    }

    private Future<SubmitDocumentChangesResult> submitUpdateStore(UpdateStore updateStore, EntityId documentId, Document document) {
        return updateStore.submitChanges(
                SubmitArgument.builder()
                        .setStatement("select set_transaction_parameters(false)")
                        .setDataSourceId(updateStore.getDataSourceId())
                        .build()
        ).compose(batch -> {
            Object documentPk = documentId.getPrimaryKey();
            Object documentRef = null;
            Object cartPk = null;
            String cartUuid = null;
            if (document != null) {
                documentPk = document.getPrimaryKey();
                documentRef = document.getRef();
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
            }
            return Future.succeededFuture(new SubmitDocumentChangesResult(documentPk, documentRef, cartPk, cartUuid));
        });
    }

    private void resolveDocumentEventsPrimaryKeys(AbstractDocumentEvent[] documentEvents, Map<EntityId, Entity> entities) {
        for (AbstractDocumentEvent e : documentEvents) {
            resolvePrimaryKeyField(Document.class, e::getDocumentPrimaryKey, e::setDocumentPrimaryKey, entities);
            if (e instanceof AbstractDocumentLineEvent) {
                AbstractDocumentLineEvent adle = (AbstractDocumentLineEvent) e;
                resolvePrimaryKeyField(DocumentLine.class, adle::getDocumentLinePrimaryKey, adle::setDocumentLinePrimaryKey, entities);
                if (e instanceof AbstractAttendancesEvent) {
                    AbstractAttendancesEvent aae = (AbstractAttendancesEvent) e;
                    Object[] attendancesPrimaryKeys = aae.getAttendancesPrimaryKeys();
                    for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
                        final int fi = i;
                        resolvePrimaryKeyField(Attendance.class, () -> attendancesPrimaryKeys[fi], pk -> attendancesPrimaryKeys[fi] = pk, entities);
                    }
                }
            }
        }
    }

    private void resolvePrimaryKeyField(Class<? extends Entity> entityClass, Supplier<Object> getter, Consumer<Object> setter, Map<EntityId, Entity> entities) {
        Object primaryKey = getter.get();
        EntityId entityId = EntityId.create(entityClass, primaryKey);
        Entity entity = entities.get(entityId);
        if (entity != null) {
            setter.accept(entity.getPrimaryKey());
        }
    }

}
