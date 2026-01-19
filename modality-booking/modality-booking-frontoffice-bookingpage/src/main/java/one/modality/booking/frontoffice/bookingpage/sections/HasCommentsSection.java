package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Interface for the "Comments and Special Requests" section of a booking form.
 * This section provides a text area for users to enter comments or special requests
 * about their registration.
 *
 * <p>The comment text is stored in the Document's request field when the booking
 * is submitted.</p>
 *
 * @author Bruno Salmon
 * @see BookingFormSection
 */
public interface HasCommentsSection extends BookingFormSection {

    /**
     * Returns the color scheme property for theming.
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     */
    @Deprecated
    ObjectProperty<BookingFormColorScheme> colorSchemeProperty();

    /**
     * Sets the color scheme for this section.
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    void setColorScheme(BookingFormColorScheme scheme);

    /**
     * Returns the user's comment text.
     */
    String getCommentText();

    /**
     * Returns the comment text property for binding.
     */
    StringProperty commentTextProperty();

    /**
     * Sets custom title text for the section header (optional).
     * If not set, uses the default i18n text.
     */
    void setTitleText(Object titleI18nKey);

    /**
     * Sets custom info text for the instruction box (optional).
     * If not set, uses the default i18n text.
     */
    void setInfoText(Object infoI18nKey);

    /**
     * Resets the section (clears the comment text).
     */
    void reset();
}
