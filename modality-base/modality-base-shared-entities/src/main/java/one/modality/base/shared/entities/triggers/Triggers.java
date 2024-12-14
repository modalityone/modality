package one.modality.base.shared.entities.triggers;

import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;

/**
 * @author Bruno Salmon
 */
public interface Triggers {

    static SubmitArgument frontOfficeTransaction(HasDataSourceModel store) {
        return SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(false)")
                .setDataSourceId(store.getDataSourceId())
                .build();
    }

    static SubmitArgument backOfficeTransaction(HasDataSourceModel store) {
        return SubmitArgument.builder()
                .setStatement("select set_transaction_parameters(true)")
                .setDataSourceId(store.getDataSourceId())
                .build();
    }
}