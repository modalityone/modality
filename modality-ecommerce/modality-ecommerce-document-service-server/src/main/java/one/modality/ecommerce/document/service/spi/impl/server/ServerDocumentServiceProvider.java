package one.modality.ecommerce.document.service.spi.impl.server;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Cart;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.*;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AddAttendancesEvent;
import one.modality.ecommerce.document.service.events.AddDocumentEvent;
import one.modality.ecommerce.document.service.events.AddDocumentLineEvent;
import one.modality.ecommerce.document.service.spi.DocumentServiceProvider;

import java.util.HashMap;
import java.util.Map;

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
        Map<Object /* PrimaryKey */, Entity> entities = new HashMap<>();
        Object documentPrimaryKey = null;
        for (AbstractDocumentEvent e : argument.getDocumentEvents()) {
            if (e instanceof AddDocumentEvent) {
                AddDocumentEvent ade = (AddDocumentEvent) e;
                Document document = updateStore.insertEntity(Document.class);
                document.setEvent(ade.getEventPrimaryKey());
                document.setPerson(ade.getPersonPrimaryKey());
                document.setFieldValue("activity", 12); // GP TODO: remove activity from DB
                entities.put(documentPrimaryKey = e.getDocumentPrimaryKey(), document);
            } else if (e instanceof AddDocumentLineEvent) {
                AddDocumentLineEvent adle = (AddDocumentLineEvent) e;
                DocumentLine documentLine = updateStore.insertEntity(DocumentLine.class);
                documentLine.setDocument(entities.get(documentPrimaryKey = adle.getDocumentPrimaryKey()));
                documentLine.setSite(adle.getSitePrimaryKey());
                documentLine.setItem(adle.getItemPrimaryKey());
                entities.put(adle.getDocumentLinePrimaryKey(), documentLine);
            } else if (e instanceof AddAttendancesEvent) {
                AddAttendancesEvent aae = (AddAttendancesEvent) e;
                for (Object sipk : aae.getScheduledItemsPrimaryKeys()) {
                    Attendance attendance = updateStore.insertEntity(Attendance.class);
                    attendance.setDocumentLine(entities.get(aae.getDocumentLinePrimaryKey()));
                    attendance.setScheduledItem(sipk);
                }
            }
        }
        Object finalDocumentPrimaryKey = documentPrimaryKey;
        return updateStore.submitChanges(
                SubmitArgument.builder()
                        .setStatement("select set_transaction_parameters(false)")
                        .setDataSourceId(dataSourceModel.getDataSourceId())
                        .build()
        ).compose(batch -> {
            Object documentPk = finalDocumentPrimaryKey;
            Object documentRef = null;
            Object cartPk = null;
            String cartUuid = null;
            Document document = (Document) entities.get(documentPk);
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

}
