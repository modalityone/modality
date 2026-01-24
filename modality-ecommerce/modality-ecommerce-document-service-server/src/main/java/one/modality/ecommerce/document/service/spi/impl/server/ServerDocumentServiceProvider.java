package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Arrays;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.triggers.Triggers;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.book.*;
import one.modality.ecommerce.document.service.events.registration.documentline.AllocateDocumentLineEvent;
import one.modality.ecommerce.document.service.events.registration.documentline.LinkMateToOwnerDocumentLineEvent;
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


    /*------------------------------------------------------------------------------------------------------------------
    |                                                  LOAD                                                            |
    ------------------------------------------------------------------------------------------------------------------*/

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

    private Future<DocumentAggregate[]> loadLatestDocumentsFromDatabase(LoadDocumentArgument argument, boolean limitTo1) {
        Object docPk = argument.documentPrimaryKey();
        EntityStoreQuery[] queries = {
            // 0 - Loading document
            new EntityStoreQuery("select event,person,ref,person_lang,person_firstName,person_lastName,person_email,person_facilityFee,request from Document where id=? order by id", docPk),
            // 1 - Loading document lines
            new EntityStoreQuery("select document,site,item,price_net,price_minDeposit,price_custom,price_discount" +
                                 ",share_owner,share_owner_mate1Name,share_owner_mate2Name,share_owner_mate3Name,share_owner_mate4Name,share_owner_mate5Name,share_owner_mate6Name,share_owner_mate7Name" +
                                 ",share_mate,share_mate_ownerName,share_mate_ownerDocumentLine,share_mate_ownerPerson" +
                                 ",resourceConfiguration,allocate" +
                                 " from DocumentLine where document=? and site!=null order by id", docPk),
            // 2 - Loading attendances
            new EntityStoreQuery("select documentLine,scheduledItem,date from Attendance where documentLine.document=? order by id", docPk),
            // 3 - Loading money transfers
            new EntityStoreQuery("select document,amount,pending,successful from MoneyTransfer where document=? order by id", docPk)
        };
        boolean personProvided = argument.personPrimaryKey() != null;
        boolean accountProvided = argument.accountPrimaryKey() != null;
        if (personProvided || accountProvided) {
            String queryReplacement = " in (select Document where !cancelled and (%field%=? and event=?) order by id desc %limit%)"
                .replace("%field%", personProvided ? "person" : "person.frontendAccount")
                .replace("%limit%", limitTo1 ? "limit 1" : "");
            Object[] queryArguments = {personProvided ? argument.personPrimaryKey() : argument.accountPrimaryKey(), argument.eventPrimaryKey()};
            if (docPk != null) {
                queryReplacement = queryReplacement.replace(") order by", " or id=?) order by");
                queryArguments = Arrays.add(Object[]::new, queryArguments, docPk);
            }
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
                ((List<DocumentLine>) entityLists[1]).forEach(documentLine -> {
                    List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(documentLine.getDocument());
                    documentEvents.add(new AddDocumentLineEvent(documentLine, documentLine.isAllocate()));
                    documentEvents.add(new PriceDocumentLineEvent(documentLine));
                    if (documentLine.isShareOwner()) {
                        documentEvents.add(new EditShareOwnerInfoDocumentLineEvent(documentLine, documentLine.getShareOwnerMatesNames()));
                    }
                    if (documentLine.isShareMate()) {
                        documentEvents.add(new EditShareMateInfoDocumentLineEvent(documentLine, documentLine.getShareMateOwnerName()));
                        DocumentLine ownerDocumentLine = documentLine.getShareMateOwnerDocumentLine();
                        Person ownerPerson = documentLine.getShareMateOwnerPerson();
                        if (ownerDocumentLine != null || ownerPerson != null) {
                            documentEvents.add(new LinkMateToOwnerDocumentLineEvent(documentLine, ownerDocumentLine, ownerPerson));
                        }
                    }
                    ResourceConfiguration resourceConfiguration = documentLine.getResourceConfiguration();
                    if (resourceConfiguration != null) {
                        documentEvents.add(new AllocateDocumentLineEvent(documentLine, resourceConfiguration));
                    }
                });
                // Aggregating attendances by adding AddAttendancesEvent for each document line
                ((List<Attendance>) entityLists[2]).stream().collect(Collectors.groupingBy(Attendance::getDocumentLine))
                    .forEach((documentLine, attendances) -> {
                        List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(documentLine.getDocument());
                        documentEvents.add(new AddAttendancesEvent(attendances.toArray(new Attendance[0])));
                    });
                // Aggregating money transfers by Adding AddMoneyTransferEvent
                ((List<MoneyTransfer>) entityLists[3]).forEach(moneyTransfer -> {
                    List<AbstractDocumentEvent> documentEvents = allDocumentEvents.get(moneyTransfer.getDocument());
                    documentEvents.add(new AddMoneyTransferEvent(moneyTransfer));
                });
                return allDocumentEvents.values().stream()
                    .map(DocumentAggregate::new)
                    .toArray(DocumentAggregate[]::new);
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



    /*------------------------------------------------------------------------------------------------------------------
    |                                                 SUBMIT                                                           |
    ------------------------------------------------------------------------------------------------------------------*/

    @Override
    public Future<SubmitDocumentChangesResult> submitDocumentChanges(SubmitDocumentChangesArgument argument) {
        DocumentSubmitRequest request = DocumentSubmitRequest.create(argument);
        if (request.document() == null)
            return Future.failedFuture("No document changes to submit");
        if (request.runId() == null) {
            String message = "No runId could be found for push notification";
            Console.log(message + " - " + argument.historyComment());
            return Future.failedFuture(message);
        }

        // Delegating the call to DocumentSubmitController, which will handle the complexity of the event queue (on big
        // events bookings openings). If no event queue is required, it will call back submitDocumentChangesNow()
        // straight away. Otherwise it will enqueue the submission for later processing but immediately return a result
        // with status ENQUEUED. At the booking opening time, the queue will process the enqueued submissions and call
        // submitDocumentChangesNow() for each of them, and communicate the final result to the clients using push
        // notification.
        return DocumentSubmitController.submitDocumentChanges(request);
    }

    static Future<SubmitDocumentChangesResult> submitDocumentChangesNow(DocumentSubmitRequest request) {
        // Note: At this point, the document may be null, but in that case we at least have documentLine not null
        return HistoryRecorder.prepareDocumentHistoriesBeforeSubmit(request.argument().historyComment(), request.document(), request.documentLine())
            .compose(histories -> // At this point, history.getDocument() is never null (it has eventually been
                submitChangesAndPrepareResult(request.updateStore(), histories[0].getDocument()) // resolved through DB reading)
                    .compose(result -> { // Completing the history recording (changes column with resolved primary keys)
                            if (result.status() == DocumentChangesStatus.APPROVED)
                                return HistoryRecorder.completeDocumentHistoriesAfterSubmit(histories, request.argument().documentEvents())
                                    .map(ignoredVoid -> result);
                            return Future.succeededFuture(result); // No technical exception, but submit refused by logic (ex: sold-out)
                        }
                    )
            );
    }

    private static Future<SubmitDocumentChangesResult> submitChangesAndPrepareResult(UpdateStore updateStore, Document document) {
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
                        .map(x -> SubmitDocumentChangesResult.createApprovedResult(
                            document.getPrimaryKey(),
                            document.getRef(),
                            document.getCart().getPrimaryKey(),
                            document.getCart().getUuid()
                        ));
                }
                return Future.succeededFuture(SubmitDocumentChangesResult.createApprovedResult(documentPk, documentRef, cartPk, cartUuid));
            })
            // Detecting sold-out exception from the database
            .recover(ex -> {
                String message = ex.getMessage();
                // If it's not a sold-out exception, we rethrow it
                if (message == null || !message.toUpperCase().contains("SoldOut".toUpperCase()))
                    return Future.failedFuture(ex);
                Object sitePk = readSiteOrItemPrimaryKey(message, true);
                Object itemPk = readSiteOrItemPrimaryKey(message, false);
                return Future.succeededFuture(SubmitDocumentChangesResult.createSoldOutResult(sitePk, itemPk));
            });
    }

    private static Object readSiteOrItemPrimaryKey(String message, boolean isSite) {
        // Here is the Postgres database code: RAISE EXCEPTION 'SOLDOUT site_id=%, item_id=% (no resource found)', NEW.site_id, NEW.item_id;
        String token = isSite ? "site_id=" : "item_id=";
        int index = message.indexOf(token);
        if (index >= 0) {
            int start = index + token.length();
            int end = start;
            while (end < message.length() && Character.isDigit(message.charAt(end))) {
                end++;
            }
            if (end > start) {
                return Integer.parseInt(message.substring(start, end));
            }
        }
        return null;
    }

    @Override
    public Future<Boolean> leaveEventQueue(Object queueToken) {
        return Future.succeededFuture(DocumentSubmitController.leaveEventQueue(queueToken));
    }

    @Override
    public Future<SubmitDocumentChangesResult> fetchEventQueueResult(Object queueToken) {
        return Future.succeededFuture(DocumentSubmitController.fetchEventQueueResult(queueToken));
    }
}
