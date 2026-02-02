package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.platform.async.Future;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

import java.util.function.Function;

/**
 * Represents a button definition for the booking form navigation.
 * Supports both synchronous actions (via EventHandler) and asynchronous actions (via Function returning Future).
 * When an async action is specified, ButtonNavigation automatically shows a spinner during execution.
 *
 * <p>For synchronous actions, use the constructors:
 * <pre>new BookingFormButton(key, e -> doSomething(), "style")</pre>
 *
 * <p>For asynchronous actions, use the static factory methods:
 * <pre>BookingFormButton.async(key, button -> asyncOperation(), "style")</pre>
 *
 * @author Bruno Salmon
 */
public class BookingFormButton {

    private final Object textI18nKey;
    private final EventHandler<ActionEvent> actionHandler;
    private final Function<Button, Future<?>> asyncActionHandler;
    private final String styleClass;
    private final ObservableValue<Boolean> disableProperty;

    // === Synchronous action constructors ===

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler) {
        this(textI18nKey, actionHandler, null, null, null);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler, String styleClass) {
        this(textI18nKey, actionHandler, null, styleClass, null);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler,
            ObservableValue<Boolean> disableProperty) {
        this(textI18nKey, actionHandler, null, null, disableProperty);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler, String styleClass,
            ObservableValue<Boolean> disableProperty) {
        this(textI18nKey, actionHandler, null, styleClass, disableProperty);
    }

    // === Asynchronous action static factory methods ===
    // Using static factory methods avoids ambiguity with EventHandler constructors

    /**
     * Creates a button with an async action handler that returns a Future.
     * The button will show a spinner and be disabled during async execution.
     *
     * @param textI18nKey The i18n key for the button text
     * @param asyncActionHandler Function that receives the button and returns a Future
     * @param styleClass CSS style class(es) for the button
     * @param disableProperty Observable property to bind button disabled state
     * @return A new BookingFormButton configured for async action
     */
    public static BookingFormButton async(Object textI18nKey, Function<Button, Future<?>> asyncActionHandler,
            String styleClass, ObservableValue<Boolean> disableProperty) {
        return new BookingFormButton(textI18nKey, null, asyncActionHandler, styleClass, disableProperty);
    }

    /**
     * Creates a button with an async action handler (no disable property).
     *
     * @param textI18nKey The i18n key for the button text
     * @param asyncActionHandler Function that receives the button and returns a Future
     * @param styleClass CSS style class(es) for the button
     * @return A new BookingFormButton configured for async action
     */
    public static BookingFormButton async(Object textI18nKey, Function<Button, Future<?>> asyncActionHandler,
            String styleClass) {
        return new BookingFormButton(textI18nKey, null, asyncActionHandler, styleClass, null);
    }

    // === Full constructor (private) ===

    private BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler,
            Function<Button, Future<?>> asyncActionHandler, String styleClass,
            ObservableValue<Boolean> disableProperty) {
        this.textI18nKey = textI18nKey;
        this.actionHandler = actionHandler;
        this.asyncActionHandler = asyncActionHandler;
        this.styleClass = styleClass;
        this.disableProperty = disableProperty;
    }

    // === Getters ===

    public Object getTextI18nKey() {
        return textI18nKey;
    }

    public EventHandler<ActionEvent> getActionHandler() {
        return actionHandler;
    }

    /**
     * Returns the async action handler if this is an async button.
     * The function receives the Button instance and should return a Future
     * that completes when the async operation is done.
     */
    public Function<Button, Future<?>> getAsyncActionHandler() {
        return asyncActionHandler;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public ObservableValue<Boolean> getDisableProperty() {
        return disableProperty;
    }

    /**
     * Returns true if this button uses an async action handler.
     * Async buttons will show a spinner during execution.
     */
    public boolean isAsync() {
        return asyncActionHandler != null;
    }
}
