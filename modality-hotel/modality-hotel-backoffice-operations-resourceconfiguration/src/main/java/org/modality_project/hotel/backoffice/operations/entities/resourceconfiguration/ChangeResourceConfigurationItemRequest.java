package org.modality_project.hotel.backoffice.operations.entities.resourceconfiguration;

import javafx.scene.layout.Pane;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.platform.async.AsyncFunction;

public final class ChangeResourceConfigurationItemRequest implements HasOperationCode,
        HasOperationExecutor<ChangeResourceConfigurationItemRequest, Void> {

    private final static String OPERATION_CODE = "ChangeResourceConfigurationItem";

    private final Entity resourceConfiguration;
    private final Pane parentContainer;
    private final String itemFamilyCode;
    private final EntityId siteId;

    public ChangeResourceConfigurationItemRequest(Entity resourceConfiguration, Pane parentContainer, String itemFamilyCode, EntityId siteId) {
        this.resourceConfiguration = resourceConfiguration;
        this.parentContainer = parentContainer;
        this.itemFamilyCode = itemFamilyCode;
        this.siteId = siteId;
    }

    Entity getResourceConfiguration() {
        return resourceConfiguration;
    }

    Pane getParentContainer() {
        return parentContainer;
    }

    String getItemFamilyCode() {
        return itemFamilyCode;
    }

    EntityId getSiteId() {
        return siteId;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<ChangeResourceConfigurationItemRequest, Void> getOperationExecutor() {
        return ChangeResourceConfigurationItemExecutor::executeRequest;
    }
}
