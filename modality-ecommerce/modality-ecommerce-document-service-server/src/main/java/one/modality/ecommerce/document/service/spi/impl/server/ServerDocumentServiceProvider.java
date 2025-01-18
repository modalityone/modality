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
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.triggers.Triggers;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;
import one.modality.ecommerce.history.server.HistoryRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class ServerDocumentServiceProvider implements DocumentServiceProvider {

    private final static String POLICY_SCHEDULED_ITEMS_QUERY_BASE = "select site.name,item.(name,code,family.code),date,startTime,timeline.startTime from ScheduledItem";
    private final static String POLICY_RATES_QUERY_BASE = "select site,item,price,perDay,perPerson,facilityFee_price,startDate,endDate from Rate";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        Object eventPk = argument.getEventPk();
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[]{
                    new QueryArgumentBuilder()
                        .setStatement(POLICY_SCHEDULED_ITEMS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=?) and bookableScheduledItem=id")
                        .setParameters(eventPk)
                        .setLanguage("DQL")
                        .setDataSourceId(DataSourceModelService.getDefaultDataSourceId())
                        .build(),
                    new QueryArgumentBuilder()
                        .setStatement(POLICY_RATES_QUERY_BASE + " where site.event = (select coalesce(repeatedEvent, id) from Event where id=?) or site = (select coalesce(repeatedEvent.venue, venue) from Event where id = ?) " +
                                      // Note: TeachingsPricing relies on the following order to work properly
                                      "order by site,item,perDay desc,startDate,endDate,price")
                        .setParameters(eventPk, eventPk)
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
        if (argument.getHistoryPrimaryKey() == null) {
            return loadLatestDocumentFromDatabase(argument);
        }
        return loadDocumentFromHistory(argument);
    }

    private Future<DocumentAggregate> loadLatestDocumentFromDatabase(LoadDocumentArgument argument) {
        Object[] parameters = {argument.getDocumentPrimaryKey()};
        EntityStoreQuery[] queries = {
            new EntityStoreQuery("select event,person,ref,person_firstName,person_lastName,person_email,person_facilityFee from Document where id=? order by id", parameters),
            new EntityStoreQuery("select document,site,item from DocumentLine where document=? and site!=null order by id", parameters),
            new EntityStoreQuery("select documentLine,scheduledItem from Attendance where documentLine.document=? order by id", parameters),
            new EntityStoreQuery("select document,amount,pending,successful from MoneyTransfer where document=? order by id", parameters)
        };
        if (parameters[0] == null) {
            parameters = new Object[]{argument.getPersonPrimaryKey(), argument.getEventPrimaryKey()};
            for (int i = 0; i < queries.length; i++) {
                queries[i] = new EntityStoreQuery(queries[i].getSelect().replace("=?", "=(select Document where person=? and event=? and !cancelled order by id desc limit 1)"), parameters);
            }
        }
        return EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .executeQueryBatch(queries)
            .map(entityLists -> {
                Document document = ((List<Document>) entityLists[0]).stream().findFirst().orElse(null);
                if (document == null) {
                    return null;
                }
                List<AbstractDocumentEvent> documentEvents = new ArrayList<>();
                documentEvents.add(new AddDocumentEvent(document));
                ((List<DocumentLine>) entityLists[1]).forEach(dl -> documentEvents.add(new AddDocumentLineEvent(dl)));
                ((List<Attendance>) entityLists[2]).stream().collect(Collectors.groupingBy(Attendance::getDocumentLine))
                    .entrySet().forEach(entry -> documentEvents.add(new AddAttendancesEvent(entry.getValue().toArray(new Attendance[0]))));
                ((List<MoneyTransfer>) entityLists[3]).forEach(mt -> documentEvents.add(new AddMoneyTransferEvent(mt)));
                if (document.isPersonFacilityFee())
                    documentEvents.add(new ApplyFacilityFeeDocumentEvent(document, true));
                return new DocumentAggregate(documentEvents);
            });
    }

    private Future<DocumentAggregate> loadDocumentFromHistory(LoadDocumentArgument argument) {
        String select = "select changes from History where document=? and id<=? order by id";
        Object[] parameters = {argument.getDocumentPrimaryKey(), argument.getHistoryPrimaryKey()};
        if (parameters[0] == null) {
            parameters = new Object[]{argument.getPersonPrimaryKey(), argument.getEventPrimaryKey(), argument.getHistoryPrimaryKey()};
            select = select.replace("document=?", "document=(select Document where person=? and event=? and !cancelled order by id desc limit 1)");
        }
        return EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .<History>executeQuery(select, parameters)
            .map(historyList -> {
                if (historyList.isEmpty()) {
                    return null;
                }
                List<AbstractDocumentEvent> documentEvents = new ArrayList<>();
                historyList.forEach(history -> {
                    ReadOnlyAstArray astArray = AST.parseArray(history.getChanges(), "json");
                    if (astArray != null) { // This case may come from KBS2
                        AbstractDocumentEvent[] events = SerialCodecManager.decodeAstArrayToJavaArray(astArray, AbstractDocumentEvent.class);
                        documentEvents.addAll(Arrays.asList(events));
                    }
                });
                return new DocumentAggregate(documentEvents);
            });
    }

    @Override
    public Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        UpdateStore updateStore = UpdateStore.create(DataSourceModelService.getDefaultDataSourceModel());
        Document document = null;
        DocumentLine documentLine = null;
        AbstractDocumentEvent[] documentEvents = argument.getDocumentEvents();
        for (AbstractDocumentEvent e : documentEvents) {
            e.setEntityStore(updateStore); // This indicates it's for submit
            e.replayEvent();
            if (document == null)
                document = e.getDocument();
            if (documentLine == null && e instanceof AbstractDocumentLineEvent) {
                documentLine = ((AbstractDocumentLineEvent) e).getDocumentLine();
            }
        }

        // Note: At this point, document may be null, but in that case we at least have documentLine not null
        return HistoryRecorder.prepareDocumentHistoryBeforeSubmit(argument.getHistoryComment(), document, documentLine)
            .compose(history -> // At this point, history.getDocument() is never null (it has eventually been
                submitChangesAndPrepareResult(updateStore, history.getDocument()) // resolved through DB reading)
                    .compose(result -> // Completing the history recording (changes column with resolved primary keys)
                        HistoryRecorder.completeDocumentHistoryAfterSubmit(result, history, argument.getDocumentEvents())
                    )
            );
    }

    private Future<SubmitDocumentChangesResult> submitChangesAndPrepareResult(UpdateStore updateStore, Document document) {
        return updateStore.submitChanges(Triggers.frontOfficeTransaction(updateStore))
            .compose(batch -> {
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
