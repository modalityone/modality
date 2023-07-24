package one.modality.base.shared.entities.converters;

import dev.webfx.stack.orm.dql.DqlClause;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.dql.DqlStatementBuilder;
import one.modality.base.shared.entities.Filter;

/**
 * @author Bruno Salmon
 */
public final class FilterToDqlStatementConverter {

  public static DqlStatement toDqlStatement(Filter filter) {
    return new DqlStatementBuilder(filter.getClassId())
        .setAlias(filter.getAlias())
        .setFields(filter.getFields())
        .setColumns(filter.getColumns())
        .setWhere(DqlClause.create(filter.getWhereClause()))
        .setGroupBy(DqlClause.create(filter.getGroupByClause()))
        .setHaving(DqlClause.create(filter.getHavingClause()))
        .setLimit(DqlClause.create(filter.getLimitClause()))
        .setOrderBy(DqlClause.create(filter.getOrderByClause()))
        .build();
  }
}
