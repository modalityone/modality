package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.entities.Person;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface for the "Member Selection" section of a booking form.
 * This section displays household members and allows selection of who to book for.
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasMemberSelectionSection extends BookingFormSection {

    /**
     * Status of a member for booking purposes.
     */
    enum MemberStatus {
        /** Member is fully validated and can be booked for */
        ACTIVE,
        /** Member invitation is pending (waiting for response) */
        PENDING_INVITATION,
        /** Member needs validation (they created their own account) */
        NEEDS_VALIDATION,
        /** Member is the account owner (always bookable) */
        OWNER
    }

    /**
     * Data class representing a household member.
     */
    class MemberInfo {
        private final Object personId;
        private final String firstName;
        private final String lastName;
        private final String name;
        private final String email;
        private final Person personEntity;
        private final MemberStatus status;

        public MemberInfo(Object personId, String name, String email, Person personEntity, MemberStatus status) {
            this.personId = personId;
            this.name = name;
            this.email = email;
            this.personEntity = personEntity;
            this.status = status;

            // Split name into first/last
            String[] parts = name != null ? name.trim().split("\\s+", 2) : new String[]{"", ""};
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        }

        public Object getPersonId() { return personId; }
        public String getName() { return name; }
        public String getEmail() { return email; }

        public Person getPersonEntity() { return personEntity; }
        public MemberStatus getStatus() { return status; }

        public boolean isBookable() {
            return status == MemberStatus.ACTIVE || status == MemberStatus.OWNER;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
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
     * Sets the callback for when a member is selected.
     */
    void setOnMemberSelected(Consumer<MemberInfo> callback);

    /**
     * Sets the callback for when the continue button is pressed.
     */
    void setOnContinuePressed(Runnable callback);

    /**
     * Sets the callback for when the back button is pressed.
     */
    void setOnBackPressed(Runnable callback);

    /**
     * Adds a single member to the list.
     */
    void addMember(MemberInfo member);

    /**
     * Clears all members from the list.
     */
    void clearMembers();

    /**
     * Clears the current selection without removing members.
     */
    void clearSelection();

    /**
     * Sets the set of person IDs already booked for this event.
     */
    void setAlreadyBookedPersonIds(Set<Object> personIds);

    /**
     * Clears all already-booked markers.
     */
    void clearAlreadyBooked();

}
