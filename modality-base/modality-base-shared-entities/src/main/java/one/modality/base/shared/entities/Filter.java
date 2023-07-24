package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.markers.EntityHasName;

public interface Filter extends Entity, EntityHasName {

  default Object getClassId() {
    return getFieldValue("class");
  }

  default String getAlias() {
    return getStringFieldValue("alias");
  }

  default String getFields() {
    return getStringFieldValue("fields");
  }

  default String getWhereClause() {
    return getStringFieldValue("whereClause");
  }

  default String getGroupByClause() {
    return getStringFieldValue("groupByClause");
  }

  default String getHavingClause() {
    return getStringFieldValue("havingClause");
  }

  default String getOrderByClause() {
    return getStringFieldValue("orderByClause");
  }

  default String getLimitClause() {
    return getStringFieldValue("limitClause");
  }

  default String getColumns() {
    return getStringFieldValue("columns");
  }

  default void setIsColumns(Boolean isColumns) {
    setFieldValue("isColumns", isColumns);
  }
}
