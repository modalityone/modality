package one.modality.booking.frontoffice.bookingpage;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;

/**
 * @author Bruno Salmon
 */
public class BookingFormButton {

    private final Object textI18nKey;
    private final EventHandler<ActionEvent> actionHandler;
    private final String styleClass;
    private final ObservableValue<Boolean> disableProperty;

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler) {
        this(textI18nKey, actionHandler, null, null);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler, String styleClass) {
        this(textI18nKey, actionHandler, styleClass, null);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler,
            ObservableValue<Boolean> disableProperty) {
        this(textI18nKey, actionHandler, null, disableProperty);
    }

    public BookingFormButton(Object textI18nKey, EventHandler<ActionEvent> actionHandler, String styleClass,
            ObservableValue<Boolean> disableProperty) {
        this.textI18nKey = textI18nKey;
        this.actionHandler = actionHandler;
        this.styleClass = styleClass;
        this.disableProperty = disableProperty;
    }

    public Object getTextI18nKey() {
        return textI18nKey;
    }

    public EventHandler<ActionEvent> getActionHandler() {
        return actionHandler;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public ObservableValue<Boolean> getDisableProperty() {
        return disableProperty;
    }

}
