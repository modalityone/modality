package one.modality.event.frontoffice.activities.booking.process.account;

import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import one.modality.base.shared.entities.Person;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;

/**
 * @author Bruno Salmon
 */
final class CheckoutAccountActivity extends ViewDomainActivityBase {

    private Person lastUserPerson;

    @Override
    public Node buildUi() { // This is what is displayed once logged-in
        return null; // new Text("Welcome!");
    }

    @Override
    public void onResume() {
        // Detecting if the user is already logged in with an existing Modality account. In that case, it means that we
        // can go back to the previous page (the page where the user was booking the event).
        Person userPerson = FXUserPerson.getUserPerson();
        boolean hasModalityAccount = userPerson != null;
        // It's important to call goBack() only once. onResume() & onPause() may be called several times on login,
        // because the redirectHandler may display some intermediate pages during this process (ex: waiting
        // authorizations, authorizations received, then going back to that page).
        boolean goingBackAlreadyCalled = userPerson == lastUserPerson;
        if (hasModalityAccount && !goingBackAlreadyCalled) {
            WindowHistory.getProvider().goBack();
        } else {
            super.onResume();
        }
        lastUserPerson = userPerson;
    }

}
