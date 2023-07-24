package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;

import one.modality.base.shared.entities.markers.EntityHasOnline;
import one.modality.base.shared.entities.markers.EntityHasSiteAndItem;
import one.modality.base.shared.entities.markers.HasName;

import java.time.LocalDate;

public interface ResourceConfiguration
        extends Entity, EntityHasSiteAndItem, EntityHasOnline, HasName {

    @Override
    default String getName() {
        return (String) evaluate("name");
    }

    @Override
    default void setName(String name) {
        setExpressionValue(parseExpression("name"), name);
    }

    default void setEndDate(LocalDate endDate) {
        setFieldValue("endDate", endDate);
    }

    default LocalDate getEndDate() {
        return getLocalDateFieldValue("endDate");
    }

    default void setLastCleaningDate(LocalDate endDate) {
        setFieldValue("lastCleaningDate", endDate);
    }

    default LocalDate getLastCleaningDate() {
        return getLocalDateFieldValue("lastCleaningDate");
    }
}
