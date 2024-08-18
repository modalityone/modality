package one.modality.crm.shared.services.authn.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.authn.UserClaims;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.session.state.client.fx.FXLoggedIn;
import dev.webfx.stack.session.state.client.fx.FXUserClaims;
import dev.webfx.stack.session.state.client.fx.FXUserId;
import javafx.beans.property.*;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class FXModalityUserPrincipal {

    private final static BooleanProperty loggedInProperty = new SimpleBooleanProperty();

    private final static ObjectProperty<ModalityUserPrincipal> modalityUserPrincipalProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Console.log("modalityUserPrincipal = " + get());
        }
    };

    public static ReadOnlyBooleanProperty loggedInProperty() {
        return loggedInProperty;
    }

    public static boolean isLoggedIn() {
        return loggedInProperty.get();
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
            ModalityUserPrincipal modalityUserPrincipal = null;
            if (userId instanceof ModalityUserPrincipal) // Happens when directly logging using Modality username/password
                modalityUserPrincipal = (ModalityUserPrincipal) userId;
            else { // User provisioning code: SSO login => ModalityUserPrincipal TODO: Move this provisioning code on server
                // Checking UserClaims (when logging through SSO)
                UserClaims userClaims = FXUserClaims.getUserClaims();
                if (userClaims != null) { // Yes, the user logged-in through SSO
                    String email = userClaims.getEmail();
                    if (email != null) { // Yes, the user provided an email
                        // Loading the Modality user (i.e. Person) owning a frontend account whose username is that exact same email
                        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
                                .<Person>executeQuery("select id, frontendAccount.id from Person where frontendAccount.(username=? and corporation=1) order by id limit 1", email)
                                .onFailure(Console::log)
                                .onSuccess(personList -> {
                                    // We should get only one matching person
                                    if (personList.size() == 1) {
                                        // Getting the Person entity
                                        Person person = personList.get(0);
                                        // Creating the ModalityUserPrincipal instance from Person entity
                                        ModalityUserPrincipal mup = new ModalityUserPrincipal(person.getPrimaryKey(), person.getForeignEntityId("frontendAccount").getPrimaryKey());
                                        // Setting FXModalityUserPrincipal with this instance
                                        setModalityUserPrincipal(mup);
                                        loggedInProperty.set(FXLoggedIn.isLoggedIn());
                                    }
                                });
                    }
                }
            }
            setModalityUserPrincipal(modalityUserPrincipal);
            loggedInProperty.set(modalityUserPrincipal != null && FXLoggedIn.isLoggedIn());
        }, FXUserId.userIdProperty(), FXUserClaims.userClaimsProperty(), FXLoggedIn.loggedInProperty());
    }

}
