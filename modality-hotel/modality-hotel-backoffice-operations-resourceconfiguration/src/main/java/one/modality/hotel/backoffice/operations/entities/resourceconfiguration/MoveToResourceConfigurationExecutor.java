package one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;

final class MoveToResourceConfigurationExecutor {

    static Future<Void> executeRequest(MoveToResourceConfigurationRequest rq) {
        return execute(rq.getResourceConfiguration(), rq.getDocumentLinePrimaryKeys());
    }

    private static Future<Void> execute(ResourceConfiguration resourceConfiguration, Object[] documentLinePrimaryKeys) {
        Promise<Void> promise = Promise.promise();
        UpdateStore updateStore = UpdateStore.create(resourceConfiguration.getStore().getDataSourceModel());
        for (Object primaryKey : documentLinePrimaryKeys) {
            DocumentLine documentLine = updateStore.getOrCreateEntity(DocumentLine.class, primaryKey);
            updateStore.updateEntity(documentLine).setResourceConfiguration(resourceConfiguration);
        }
        // Commented as now automatically set by the Dql submit interceptor TODO Remove this comment once the feature is completed
        //updateStore.setSubmitScope(AggregateScope.builder().addAggregate("ResourceConfiguration", resourceConfiguration.getPrimaryKey()).build());
        updateStore.submitChanges()
                .onSuccess(result -> promise.complete())
                .onFailure(cause -> {
                    promise.fail(cause);
                    cause.printStackTrace();
                });
        return promise.future();
    }
}
