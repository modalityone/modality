package org.modality_project.ecommerce.client.businessdata.workingdocument;

import dev.webfx.platform.shared.async.AsyncUtil;
import org.modality_project.base.client.aggregates.cart.CartAggregateImpl;
import org.modality_project.base.client.aggregates.event.EventAggregate;
import org.modality_project.base.shared.entities.Attendance;
import org.modality_project.base.shared.entities.Document;
import org.modality_project.base.shared.entities.DocumentLine;
import dev.webfx.framework.shared.orm.entity.EntityList;
import dev.webfx.framework.shared.orm.entity.EntityStore;
import dev.webfx.framework.shared.orm.entity.EntityStoreQuery;
import dev.webfx.platform.shared.async.Future;
import dev.webfx.platform.shared.util.collection.Collections;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class WorkingDocumentLoader {

    public static Future<WorkingDocument> load(Document document) {
        return load(EventAggregate.getOrCreateFromDocument(document), document.getPrimaryKey());
    }

    public static Future<WorkingDocument> load(EventAggregate eventAggregate, Object documentPk) {
        EntityStore store = EntityStore.createAbove(eventAggregate.getEventStore());
        Future<EntityList[]> queryBatchFuture = store.executeQueryBatch(
              new EntityStoreQuery(CartAggregateImpl.DOCUMENT_LINE_LOAD_QUERY, new Object[]{documentPk})
            , new EntityStoreQuery(CartAggregateImpl.ATTENDANCE_LOAD_QUERY   , new Object[]{documentPk})
        );
        return AsyncUtil.allOf(eventAggregate.onEventOptions(), queryBatchFuture).map(v -> {
            EntityList[] entityLists = queryBatchFuture.result();
            EntityList<DocumentLine> dls = entityLists[0];
            EntityList<Attendance> as = entityLists[1];
            List<WorkingDocumentLine> wdls = new ArrayList<>();
            for (DocumentLine dl : dls)
                wdls.add(new WorkingDocumentLine(dl, Collections.filter(as, a -> a.getDocumentLine() == dl), eventAggregate));
            WorkingDocument loadedWorkingDocument = new WorkingDocument(eventAggregate, store.getEntity(Document.class, documentPk), wdls);
            return new WorkingDocument(loadedWorkingDocument);
        });
    }

}
