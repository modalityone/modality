package one.modality.base.shared.entities.util;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class DocumentLines {

    public static boolean isOfFamily(DocumentLine documentLine, KnownItemFamily family) {
        return Items.isOfFamily(documentLine, family);
    }

    public static Stream<DocumentLine> filterFamily(Stream<DocumentLine> documentLines, KnownItemFamily family) {
        return documentLines.filter(documentLine -> isOfFamily(documentLine, family));
    }

    public static List<DocumentLine> filterFamily(List<DocumentLine> documentLines, KnownItemFamily family) {
        return Collections.filter(documentLines, documentLine -> isOfFamily(documentLine, family));
    }

    public static List<DocumentLine> fromAttendances(List<Attendance> attendances) {
        return Collections.map(attendances, Attendance::getDocumentLine);
    }

    public static boolean isFreeOfCharge(DocumentLine documentLine) {
        if (documentLine == null)
            return false;
        Integer priceCustom = documentLine.getPriceCustom();
        if (priceCustom != null)
            return priceCustom == 0;
        Integer priceDiscount = documentLine.getPriceDiscount();
        if (priceDiscount != null)
            return priceDiscount == 100;
        return false;
    }


}
