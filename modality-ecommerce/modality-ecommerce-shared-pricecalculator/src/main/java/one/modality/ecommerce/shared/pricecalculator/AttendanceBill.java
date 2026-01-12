package one.modality.ecommerce.shared.pricecalculator;

import one.modality.base.shared.entities.DocumentLine;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class AttendanceBill {

    private final DocumentLine documentLine;
    private final LocalDate date;
    int price;

    AttendanceBill(DocumentLine documentLine, LocalDate date) {
        this.documentLine = documentLine;
        this.date = date;
    }

    LocalDate getDate() {
        return date;
    }

    public DocumentLine getDocumentLine() {
        return documentLine;
    }

    public int getPrice() {
        return price;
    }
}
