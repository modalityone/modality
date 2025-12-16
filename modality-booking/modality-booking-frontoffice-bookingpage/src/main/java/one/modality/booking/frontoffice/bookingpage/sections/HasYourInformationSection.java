package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.entities.Person;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.function.Consumer;

/**
 * Interface for the "Your Information" section of a booking form.
 * This section handles login/registration using an email-first flow.
 *
 * <p>The section typically has multiple states:</p>
 * <ul>
 *   <li>EMAIL_INPUT - Initial email entry</li>
 *   <li>EXISTING_USER - Login with password</li>
 *   <li>NEW_USER - Registration form</li>
 *   <li>FORGOT_PASSWORD - Password recovery</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasYourInformationSection extends BookingFormSection {

    /**
     * Data class for new user registration information.
     */
    class NewUserData {
        public final String email;
        public final String firstName;
        public final String lastName;
        public final boolean createAccount;

        public NewUserData(String email, String firstName, String lastName, boolean createAccount) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.createAccount = createAccount;
        }
    }

    /**
     * Returns the color scheme property for theming.
     */
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     */
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Sets the callback for when an existing user successfully logs in.
     *
     * @param callback receives the logged-in Person entity
     */
    void setOnLoginSuccess(Consumer<Person> callback);

    /**
     * Sets the callback for when a new user continues without an account.
     *
     * @param callback receives the new user's entered data
     */
    void setOnNewUserContinue(Consumer<NewUserData> callback);

    /**
     * Sets the callback for when the back button is pressed.
     */
    void setOnBackPressed(Runnable callback);

    /**
     * Returns the email entered by the user.
     */
    String getEmail();

    /**
     * Returns the first name entered by a new user.
     */
    String getFirstName();

    /**
     * Returns the last name entered by a new user.
     */
    String getLastName();

    /**
     * Returns whether the user wants to create an account.
     */
    boolean isCreateAccount();

    /**
     * Resets the section to initial email input state.
     */
    void resetToEmailInput();

    /**
     * Navigates back within the section's internal state machine.
     */
    void goBack();
}
