package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.beans.property.*;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class FXModalityUserPrincipal {

    private final static BooleanProperty loggedInProperty = new SimpleBooleanProperty();

    private final static ObjectProperty<ModalityUserPrincipal> modalityUserPrincipalProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            loggedInProperty.setValue(get() != null);
        }
    };

    public static ReadOnlyBooleanProperty loggedInProperty() {
        return loggedInProperty;
    }


    public static ReadOnlyObjectProperty<ModalityUserPrincipal> modalityUserPrincipalProperty() {
        return modalityUserPrincipalProperty;
    }

    private static void setModalityUserPrincipal(ModalityUserPrincipal userPrincipal) {
        modalityUserPrincipalProperty.setValue(userPrincipal);
    }

    public static ModalityUserPrincipal getModalityUserPrincipal() {
        return modalityUserPrincipalProperty().getValue();
    }

    static {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object userId = FXUserId.getUserId();
            if (userId instanceof ModalityUserPrincipal)
                setModalityUserPrincipal((ModalityUserPrincipal) userId);
        }, FXUserId.userIdProperty());
    }

}
