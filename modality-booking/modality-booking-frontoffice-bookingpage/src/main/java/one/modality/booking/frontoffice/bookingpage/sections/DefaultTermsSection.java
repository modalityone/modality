package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.windowlocation.WindowLocation;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * Default implementation of the Terms and Conditions section.
 * Displays a checkbox with terms acceptance text and a link to the terms page.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-terms-section} - section container</li>
 *   <li>{@code .booking-form-terms-checkbox} - checkbox row</li>
 *   <li>{@code .booking-form-terms-link} - terms hyperlink</li>
 *   <li>{@code .booking-form-terms-error} - error message</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasTermsSection
 */
public class DefaultTermsSection implements HasTermsSection {

    // === PROPERTIES ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final SimpleBooleanProperty termsAcceptedProperty = new SimpleBooleanProperty(false);
    protected final StringProperty termsUrlProperty = new SimpleStringProperty("");
    protected final StringProperty customTermsTextProperty = new SimpleStringProperty(null);

    // Validation - section is valid only when terms are accepted
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === UI COMPONENTS ===
    protected final VBox container = new VBox(12);
    protected HBox checkboxRow;
    protected Label errorLabel;
    protected boolean showError = false;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultTermsSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        // Checkbox row with terms text and link
        checkboxRow = createTermsCheckboxRow();

        // Error message (hidden by default)
        errorLabel = I18nControls.newLabel(BookingPageI18nKeys.TermsRequired);
        errorLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-danger");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        container.getChildren().addAll(checkboxRow, errorLabel);
        container.getStyleClass().add("booking-form-terms-section");
        container.setPadding(new Insets(0));
    }

    protected void setupBindings() {
        // Valid when terms are accepted
        validProperty.bind(termsAcceptedProperty);

        // Hide error when terms are accepted
        termsAcceptedProperty.addListener((obs, old, accepted) -> {
            if (accepted && showError) {
                hideError();
            }
        });
    }

    protected HBox createTermsCheckboxRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setCursor(Cursor.HAND);
        row.getStyleClass().addAll("booking-form-terms-checkbox", "bookingpage-checkbox-card");
        row.setPadding(new Insets(16, 20, 16, 16));

        // Toggle 'selected' class based on terms accepted state
        Runnable updateSelectedClass = () -> {
            if (termsAcceptedProperty.get()) {
                if (!row.getStyleClass().contains("selected")) {
                    row.getStyleClass().add("selected");
                }
            } else {
                row.getStyleClass().remove("selected");
            }
        };
        // Set initial state
        updateSelectedClass.run();
        // Listen for changes
        termsAcceptedProperty.addListener((obs, old, accepted) -> updateSelectedClass.run());

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(termsAcceptedProperty);

        // Text flow with terms text and link
        TextFlow textFlow = createTermsTextFlow();
        HBox.setHgrow(textFlow, Priority.ALWAYS);

        row.getChildren().addAll(checkbox, textFlow);

        // Click on row toggles checkbox (but not on the hyperlink)
        row.setOnMouseClicked(e -> {
            if (!(e.getTarget() instanceof Hyperlink)) {
                termsAcceptedProperty.set(!termsAcceptedProperty.get());
            }
        });

        return row;
    }

    protected TextFlow createTermsTextFlow() {
        TextFlow textFlow = new TextFlow();

        // "I have read and accept the "
        Label prefixLabel = I18nControls.newLabel(BookingPageI18nKeys.AcceptTermsText);
        prefixLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-dark");

        // Space between prefix and link
        Label spaceLabel = new Label(" ");

        // "terms and conditions" link
        Hyperlink termsLink = I18nControls.newHyperlink(BookingPageI18nKeys.TermsLinkText);
        termsLink.getStyleClass().addAll("booking-form-terms-link", "bookingpage-text-sm");
        termsLink.setOnAction(e -> openTermsUrl());

        textFlow.getChildren().addAll(prefixLabel, spaceLabel, termsLink);
        return textFlow;
    }

    protected void openTermsUrl() {
        String url = termsUrlProperty.get();
        if (url != null && !url.isEmpty()) {
            // Use WindowLocation to open in a new tab (GWT-compatible)
            WindowLocation.assignHref(url);
        }
    }

    /**
     * Hides the error message.
     */
    public void hideError() {
        showError = false;
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        checkboxRow.getStyleClass().remove("error");
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.TermsAndConditions;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties props) {
        this.workingBookingProperties = props;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // ========================================
    // HasTermsSection INTERFACE
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
    public boolean isTermsAccepted() {
        return termsAcceptedProperty.get();
    }

    @Override
    public BooleanProperty termsAcceptedProperty() {
        return termsAcceptedProperty;
    }

    @Override
    public void setTermsUrl(String url) {
        termsUrlProperty.set(url);
    }

    @Override
    public StringProperty termsUrlProperty() {
        return termsUrlProperty;
    }

    @Override
    public void setTermsText(String text) {
        customTermsTextProperty.set(text);
        // If custom text is set, rebuild the checkbox row
        if (text != null) {
            rebuildCheckboxRow();
        }
    }

    protected void rebuildCheckboxRow() {
        container.getChildren().remove(checkboxRow);
        checkboxRow = createTermsCheckboxRow();
        container.getChildren().add(0, checkboxRow);
    }

    @Override
    public void reset() {
        termsAcceptedProperty.set(false);
        hideError();
    }
}
