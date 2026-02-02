package one.modality.ecommerce.backoffice.operations.entities.moneyaccount;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.extras.operation.HasOperationExecutor;
import dev.webfx.platform.async.AsyncFunction;
import javafx.scene.layout.Pane;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.Organization;

public final class AddNewMoneyAccountRequest implements HasOperationCode, HasI18nKey,
        HasOperationExecutor<AddNewMoneyAccountRequest, Void> {

    private final static String OPERATION_CODE = "AddNewMoneyAccount";

    private final Organization organization;
    private final Pane parentContainer;

    public AddNewMoneyAccountRequest(Organization organization, Pane parentContainer) {
        this.organization = organization;
        this.parentContainer = parentContainer;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return I18nKeys.appendEllipsis(BaseI18nKeys.Add);
    }

    public Organization getOrganization() {
        return organization;
    }

    public Pane getParentContainer() {
        return parentContainer;
    }

    @Override
    public AsyncFunction<AddNewMoneyAccountRequest, Void> getOperationExecutor() {
        return AddNewMoneyAccountExecutor::executeRequest;
    }
}
