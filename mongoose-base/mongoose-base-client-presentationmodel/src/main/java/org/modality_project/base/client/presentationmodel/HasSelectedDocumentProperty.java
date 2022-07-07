package org.modality_project.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;
import org.modality_project.base.shared.entities.Document;

public interface HasSelectedDocumentProperty {

    ObjectProperty<Document> selectedDocumentProperty();

    default Document getSelectedDocument() { return selectedDocumentProperty().getValue(); }

    default void setSelectedDocument(Document value) { selectedDocumentProperty().setValue(value); }

}
