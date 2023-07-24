package one.modality.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;

public interface HasGanttSelectedObjectProperty {

  ObjectProperty<Object> ganttSelectedObjectProperty();

  default Object getGanttSelectedObject() {
    return ganttSelectedObjectProperty().getValue();
  }

  default void setGanttSelectedObject(Object value) {
    ganttSelectedObjectProperty().setValue(value);
  }
}
