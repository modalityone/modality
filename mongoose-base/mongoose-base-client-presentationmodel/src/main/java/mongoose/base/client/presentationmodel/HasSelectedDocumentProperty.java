package mongoose.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;
import mongoose.base.shared.entities.Document;

public interface HasSelectedDocumentProperty {

    ObjectProperty<Document> selectedDocumentProperty();

    default Document getSelectedDocument() { return selectedDocumentProperty().getValue(); }

    default void setSelectedDocument(Document value) { selectedDocumentProperty().setValue(value); }

}
