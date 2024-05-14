package one.modality.ecommerce.document.service;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public final class LoadDocumentArgument {

    private final Object primaryKey;
    private final LocalDateTime dateTime;

    public LoadDocumentArgument(Object primaryKey, LocalDateTime dateTime) {
        this.primaryKey = primaryKey;
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }
}
