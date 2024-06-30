package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.*;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;
import one.modality.ecommerce.history.server.HistoryRecorder;

import java.util.ArrayList;
import java.util.List;

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

        // Note: At this point, document may be null, but in that case we at least have documentLine not null
        return HistoryRecorder.prepareDocumentHistoryBeforeSubmit(argument.getHistoryComment(), document, documentLine)
                .compose(history -> // At this point, history.getDocument() is never null (it has eventually been
                        submitChangesAndPrepareResult(updateStore, history.getDocument()) // resolved through DB reading)
                        .onSuccess(ignored -> // Completing the history recording (changes column with resolved primary keys)
                            HistoryRecorder.completeDocumentHistoryAfterSubmit(history, argument.getDocumentEvents())
                        )
                );
    }

    private Future<SubmitDocumentChangesResult> submitChangesAndPrepareResult(UpdateStore updateStore, Document document) {
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

}
