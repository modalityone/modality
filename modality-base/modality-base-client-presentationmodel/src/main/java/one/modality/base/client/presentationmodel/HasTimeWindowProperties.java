package one.modality.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;

/**
 * @author Bruno Salmon
 */
public interface HasTimeWindowProperties<T> {

    ObjectProperty<T> timeWindowStartProperty();

    default T getTimeWindowStart() { return timeWindowStartProperty().getValue(); }

    default void setTimeWindowStart(T value) { timeWindowStartProperty().setValue(value); }

    ObjectProperty<T> timeWindowEndProperty();

    default T getTimeWindowEnd() { return timeWindowEndProperty().getValue(); }

    default void setTimeWindowEnd(T value) { timeWindowEndProperty().setValue(value); }



}
