package org.modality_project.base.shared.entities.converters;

import dev.webfx.framework.shared.orm.dql.DqlClause;
import dev.webfx.framework.shared.orm.dql.DqlStatement;
import dev.webfx.framework.shared.orm.dql.DqlStatementBuilder;
import org.modality_project.base.shared.entities.Filter;

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
