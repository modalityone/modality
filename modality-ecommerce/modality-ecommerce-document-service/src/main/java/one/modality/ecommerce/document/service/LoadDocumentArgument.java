package one.modality.ecommerce.document.service;

import dev.webfx.stack.orm.entity.Entities;

/**
 * @param documentPrimaryKey Primary key of the document to load
 * @param eventPrimaryKey    Primary key of the event to load the document from (if documentPrimaryKey is not provided)
 * @param personPrimaryKey   Alternatively, the primary key of the person and event. In that case, the document service will load the latest document associated with that person for this event (or null if none is found).
 * @param accountPrimaryKey
 * @param historyPrimaryKey  Optional history loading. If not null, it will load the document up to that history. If null, it will load the latest version of the document.
 * @author Bruno Salmon
 */

public record LoadDocumentArgument(
    Object documentPrimaryKey,
    Object eventPrimaryKey,
    Object personPrimaryKey,
    Object accountPrimaryKey,
    Object historyPrimaryKey) {

    public static LoadDocumentArgument ofDocument(Object document) {
        return ofDocumentFromHistory(document, null);
    }

    public static LoadDocumentArgument ofDocumentFromHistory(Object document, Object history) {
        return new LoadDocumentArgument(Entities.getPrimaryKey(document), null, null, null, Entities.getPrimaryKey(history));
    }

    public static LoadDocumentArgument ofPerson(Object person, Object event) {
        return new LoadDocumentArgument(null, Entities.getPrimaryKey(event), Entities.getPrimaryKey(person), null, null);
    }

    public static LoadDocumentArgument ofAccount(Object account, Object event) {
        return new LoadDocumentArgument(null, Entities.getPrimaryKey(event), null, Entities.getPrimaryKey(account), null);
    }

    public static LoadDocumentArgument ofDocumentOrAccount(Object document, Object account, Object event) {
        return new LoadDocumentArgument(Entities.getPrimaryKey(document), Entities.getPrimaryKey(event), null, Entities.getPrimaryKey(account), null);
    }

}
