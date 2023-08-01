package one.modality.crm.shared.services.authn.fx;

import dev.webfx.stack.session.state.client.fx.FXUserPrincipal;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        FXUserPrincipal.userPrincipalProperty().addListener((observable, oldValue, userPrincipal) -> {
            if (userPrincipal instanceof ModalityUserPrincipal)
                setModalityUserPrincipal((ModalityUserPrincipal) userPrincipal);
        });
    }

}
