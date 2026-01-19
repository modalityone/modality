package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Default implementation of the "Comments and Special Requests" section.
 * Provides a text area for users to enter comments or special requests about their registration.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-input-bordered} - text area border styling</li>
 *   <li>{@code .bookingpage-input-focused} - focused state styling</li>
 *   <li>{@code .bookingpage-text-base} - base text styling</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasCommentsSection
 */
public class DefaultCommentsSection implements HasCommentsSection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final StringProperty commentTextProperty = new SimpleStringProperty("");

    // === I18N KEYS ===
    protected Object titleI18nKey = BookingPageI18nKeys.CommentsAndSpecialRequests;
    protected Object infoI18nKey = BookingPageI18nKeys.CommentsAndSpecialRequestsInfo;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox(16);
    protected TextArea commentTextArea;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultCommentsSection() {
        buildUI();
    }

    protected void buildUI() {
        container.setPadding(new Insets(24, 0, 0, 0));

        // Section header using StyledSectionHeader (consistent with other sections)
        HBox sectionHeader = new StyledSectionHeader(
            titleI18nKey,
            StyledSectionHeader.ICON_CLIPBOARD
        );

        // Info box with grey background
        HBox infoBox = BookingPageUIBuilder.createInfoBox(
            infoI18nKey,
            BookingPageUIBuilder.InfoBoxType.NEUTRAL
        );
        VBox.setMargin(infoBox, new Insets(8, 0, 0, 0));

        // TextArea for user input
        commentTextArea = new TextArea();
        // Clear ALL inherited style classes to remove default black border from WebFX/GWT
        commentTextArea.getStyleClass().clear();
        commentTextArea.setWrapText(true);
        commentTextArea.setPrefRowCount(4);
        commentTextArea.setMaxWidth(Double.MAX_VALUE);
        commentTextArea.setPadding(new Insets(14, 16, 14, 16));
        // Add light gray border + text styling
        commentTextArea.getStyleClass().addAll("bookingpage-input-bordered", "bookingpage-text-base");
        // Bind text property
        commentTextArea.textProperty().bindBidirectional(commentTextProperty);
        VBox.setMargin(commentTextArea, new Insets(8, 0, 0, 0));

        // Assemble the container
        container.getChildren().addAll(sectionHeader, infoBox, commentTextArea);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return titleI18nKey;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
    }

    // ========================================
    // HasCommentsSection INTERFACE
    // ========================================

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public String getCommentText() {
        return commentTextProperty.get();
    }

    @Override
    public StringProperty commentTextProperty() {
        return commentTextProperty;
    }

    @Override
    public void setTitleText(Object titleI18nKey) {
        this.titleI18nKey = titleI18nKey;
        // Rebuild UI if already built
        if (!container.getChildren().isEmpty()) {
            container.getChildren().clear();
            buildUI();
        }
    }

    @Override
    public void setInfoText(Object infoI18nKey) {
        this.infoI18nKey = infoI18nKey;
        // Rebuild UI if already built
        if (!container.getChildren().isEmpty()) {
            container.getChildren().clear();
            buildUI();
        }
    }

    @Override
    public void reset() {
        commentTextProperty.set("");
    }
}
