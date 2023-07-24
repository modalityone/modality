package one.modality.base.client.presentationmodel;

import javafx.beans.property.ObjectProperty;

import one.modality.base.shared.entities.DocumentLine;

public interface HasSelectedDocumentLineProperty {

    ObjectProperty<DocumentLine> selectedDocumentLineProperty();

    default DocumentLine getSelectedDocumentLine() {
        return selectedDocumentLineProperty().getValue();
    }

    default void setSelectedDocumentLine(DocumentLine value) {
        selectedDocumentLineProperty().setValue(value);
    }
}
