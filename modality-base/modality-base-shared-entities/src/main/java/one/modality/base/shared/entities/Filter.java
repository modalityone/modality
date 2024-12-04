package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.EntityHasName;
import dev.webfx.stack.orm.entity.Entity;

public interface Filter extends Entity, EntityHasName {
    String classId = "class";
    String alias = "alias";
    String fields = "fields";
    String whereClause = "whereClause";
    String groupByClause = "groupByClause";
    String havingClause = "havingClause";
    String orderByClause = "orderByClause";
    String limitClause = "limitClause";
    String columns = "columns";
    String isColumns = "isColumns";

    default Object getClassId() {
        return getFieldValue(classId);
    }

    default String getAlias() {
        return getStringFieldValue(alias);
    }

    default String getFields() {
        return getStringFieldValue(fields);
    }

    default String getWhereClause() {
        return getStringFieldValue(whereClause);
    }

    default String getGroupByClause() {
        return getStringFieldValue(groupByClause);
    }

    default String getHavingClause() {
        return getStringFieldValue(havingClause);
    }

    default String getOrderByClause() {
        return getStringFieldValue(orderByClause);
    }

    default String getLimitClause() {
        return getStringFieldValue(limitClause);
    }

    default String getColumns() {
        return getStringFieldValue(columns);
    }

    default void setIsColumns(Boolean value) {
        setFieldValue(isColumns, value);
    }
}