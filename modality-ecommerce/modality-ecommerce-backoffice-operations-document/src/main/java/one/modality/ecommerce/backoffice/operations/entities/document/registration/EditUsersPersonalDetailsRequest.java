package one.modality.ecommerce.backoffice.operations.entities.document.registration;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.HasOperationExecutor;
import one.modality.base.shared.entities.Person;

public final class EditUsersPersonalDetailsRequest implements HasOperationCode,
        HasOperationExecutor<EditUsersPersonalDetailsRequest, Void> {

    private final static String OPERATION_CODE = "EditUsersPersonalDetails";

    private final Person person;

    private final ButtonSelectorParameters buttonSelectorParameters;

    public EditUsersPersonalDetailsRequest(Person person, ButtonSelectorParameters buttonSelectorParameters) {
        this.person = person;
        this.buttonSelectorParameters = buttonSelectorParameters;
    }

    Person getPerson() {
        return person;
    }

    public ButtonSelectorParameters getButtonSelectorParameters() {
        return buttonSelectorParameters;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public AsyncFunction<EditUsersPersonalDetailsRequest, Void> getOperationExecutor() {
        return EditUsersPersonalDetailsExecutor::executeRequest;
    }
}
