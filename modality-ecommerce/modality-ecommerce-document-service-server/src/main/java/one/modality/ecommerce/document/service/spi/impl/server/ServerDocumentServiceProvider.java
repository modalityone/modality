package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.DqlQueries;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.triggers.Triggers;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;
import one.modality.ecommerce.history.server.HistoryRecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class ServerDocumentServiceProvider implements DocumentServiceProvider {

    private final static String POLICY_SCHEDULED_ITEMS_QUERY_BASE = "select site.name,item.(name,label,code,family.(code,name,label),ord),date,startTime,timeline.startTime,cancelled from ScheduledItem";
    private final static String POLICY_RATES_QUERY_BASE = "select site,item,price,perDay,perPerson,facilityFee_price,startDate,endDate,onDate,offDate,minDeposit,cutoffDate,minDeposit2,age1_max,age1_price,age1_discount,age2_max,age2_price,age2_discount,resident_price,resident_discount,resident2_price,resident2_discount from Rate";
    private final static String POLICY_BOOKABLE_PERIODS_QUERY_BASE = "select startScheduledItem,endScheduledItem,name,label from BookablePeriod";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        Object eventPk = argument.getEventPk();
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[]{
                    // Loading scheduled items (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_SCHEDULED_ITEMS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=$1) and bookableScheduledItem=id " +
                        "order by site,item,date", eventPk),
                    // Loading rates (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_RATES_QUERY_BASE + " where site.event = (select coalesce(repeatedEvent, id) from Event where id=$1) or site = (select coalesce(repeatedEvent.venue, venue) from Event where id=$1) " +
                        // Note: TeachingsPricing relies on the following order to work properly
                        "order by site,item,perDay desc,startDate,endDate,price", eventPk),
                    // Loading bookable periods (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_BOOKABLE_PERIODS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=$1)" +
                        "order by startScheduledItem.date,endScheduledItem.date", eventPk)
                }))
            .map(batch -> new PolicyAggregate(
                POLICY_SCHEDULED_ITEMS_QUERY_BASE, batch.get(0),
                POLICY_RATES_QUERY_BASE, batch.get(1),
                POLICY_BOOKABLE_PERIODS_QUERY_BASE, batch.get(2)));
    }

    @Override
    public Future<DocumentAggregate> loadDocument(LoadDocumentArgument argument) {
        if (argument.historyPrimaryKey() == null) {
            return loadLatestDocumentsFromDatabase(argument, true)
                .map(Arrays::first);
        }
        return loadDocumentFromHistory(argument);
    }

    @Override
    public Future<DocumentAggregate[]> loadDocuments(LoadDocumentArgument argument) {
        return loadLatestDocumentsFromDatabase(argument, false);
    }

    private Future<DocumentAggregate[]> loadLatestDocumentsFromDatabase(LoadDocumentArgument argument, boolean limit1) {
        Object docPk = argument.documentPrimaryKey();
        EntityStoreQuery[] queries = {
            new EntityStoreQuery("select event,person,ref,person_lang,person_firstName,person_lastName,person_email,person_facilityFee,request from Document where id=? order by id", docPk),
            new EntityStoreQuery("select document,site,item,price_net,price_minDeposit,price_custom,price_discount from DocumentLine where document=? and site!=null order by id", docPk),
            new EntityStoreQuery("select documentLine,scheduledItem from Attendance where documentLine.document=? order by id", docPk),
            new EntityStoreQuery("select document,amount,pending,successful from MoneyTransfer where document=? order by id", docPk)
        };
        if (docPk == null) {
            boolean personProvided = argument.personPrimaryKey() != null;
            String queryReplacement = "in (select Document where %field%=? and event=? and !cancelled order by id desc %limit%)"
                .replace("%field%", personProvided ? "person" : "person.frontendAccount")
                .replace("%limit%", limit1 ? "limit 1" : "");
            Object[] queryArguments = {personProvided ? argument.personPrimaryKey() : argument.accountPrimaryKey(), argument.eventPrimaryKey()};
            for (int i = 0; i < queries.length; i++) {
                queries[i] = new EntityStoreQuery(queries[i].getSelect().replace("=?", queryReplacement), queryArguments);
            }
        }
        return EntityStore.create()
            .executeQueryBatch(queries)
            .map(entityLists -> {
                Map<Document, List<AbstractDocumentEvent>> allDocumentEvents = new HashMap<>();
                // Creating initial document events (with AddDocumentEvent only)
                Collections.forEach((List<Document>) entityLists[0], document -> {
                    List<AbstractDocumentEvent> documentEvents = new ArrayList<>();
                    documentEvents.add(new AddDocumentEvent(document));
                    allDocumentEvents.put(document, documentEvents);
                    if (document.isPersonFacilityFee())
                        documentEvents.add(new ApplyFacilityFeeEvent(document, true));
                    if (!Strings.isBlank(document.getRequest()))
                        documentEvents.add(new AddRequestEvent(document, document.getRequest()));
                });
                // Aggregating document lines by adding AddDocumentLineEvent and PriceDocumentLineEvent for each document
                ((List<DocumentLine>) entityLists[1]).forEach(dl -> {
                    List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(dl.getDocument());
                    documentEvents.add(new AddDocumentLineEvent(dl));
                    documentEvents.add(new PriceDocumentLineEvent(dl));
                });
                // Aggregating attendances by adding AddAttendancesEvent for each document line
                ((List<Attendance>) entityLists[2]).stream().collect(Collectors.groupingBy(Attendance::getDocumentLine))
                    .entrySet().forEach(entry -> {
                        List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(entry.getKey().getDocument());
                        documentEvents.add(new AddAttendancesEvent(entry.getValue().toArray(new Attendance[0])));
                    });
                // Aggregating money transfers by Adding AddMoneyTransferEvent
                ((List<MoneyTransfer>) entityLists[3]).forEach(mt -> {
                    List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(mt.getDocument());
                    documentEvents.add(new AddMoneyTransferEvent(mt));
                });
                return allDocumentEvents.values().stream().map(DocumentAggregate::new).toArray(DocumentAggregate[]::new);
            });
    }

    private Future<DocumentAggregate> loadDocumentFromHistory(LoadDocumentArgument argument) {
        String select = "select changes from History where document=? and id<=? order by id";
        Object[] parameters = {argument.documentPrimaryKey(), argument.historyPrimaryKey()};
        if (parameters[0] == null) {
            parameters = new Object[]{argument.personPrimaryKey(), argument.eventPrimaryKey(), argument.historyPrimaryKey()};
            select = select.replace("document=?", "document=(select Document where person=? and event=? and !cancelled order by id desc limit 1)");
        }
        return EntityStore.create()
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
            e.setEntityStore(updateStore); // This indicates it's for submission
            e.replayEvent();
            if (document == null)
                document = e.getDocument();
            if (documentLine == null && e instanceof AbstractDocumentLineEvent) {
                documentLine = ((AbstractDocumentLineEvent) e).getDocumentLine();
            }
        }

        if (document == null && documentLine == null)
            return Future.failedFuture("No document changes to submit");

        // Note: At this point, the document may be null, but in that case we at least have documentLine not null
        return HistoryRecorder.prepareDocumentHistoriesBeforeSubmit(argument.getHistoryComment(), document, documentLine)
            .compose(histories -> // At this point, history.getDocument() is never null (it has eventually been
                submitChangesAndPrepareResult(updateStore, histories[0].getDocument()) // resolved through DB reading)
                    .compose(result -> // Completing the history recording (changes column with resolved primary keys)
                        HistoryRecorder.completeDocumentHistoriesAfterSubmit(histories, argument.getDocumentEvents())
                            .map(ignoredVoid -> result)
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
