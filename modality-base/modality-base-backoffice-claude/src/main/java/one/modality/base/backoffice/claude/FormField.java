package one.modality.base.backoffice.claude;

import javafx.scene.layout.VBox;

/**
 * Container for form field components with type-safe access to the input field.
 */
@SuppressWarnings("unusable-by-js")
public record FormField<T>(VBox container, T inputField) {
}
