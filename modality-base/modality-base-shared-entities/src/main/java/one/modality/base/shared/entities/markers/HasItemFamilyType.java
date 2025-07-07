package one.modality.base.shared.entities.markers;

import one.modality.base.shared.knownitems.KnownItemFamily;

/**
 * @author Bruno Salmon
 */
public interface HasItemFamilyType {

    KnownItemFamily getItemFamilyType();

    default boolean isAccommodation() {
        return getItemFamilyType() == KnownItemFamily.ACCOMMODATION;
    }

    default boolean isMeals() {
        return getItemFamilyType() == KnownItemFamily.MEALS;
    }

    default boolean isDiet() {
        return getItemFamilyType() == KnownItemFamily.DIET;
    }

    default boolean isTeaching() {
        return getItemFamilyType() == KnownItemFamily.TEACHING;
    }

    default boolean isTranslation() {
        return getItemFamilyType() == KnownItemFamily.TRANSLATION;
    }

    default boolean isTransport() {
        return getItemFamilyType() == KnownItemFamily.TRANSPORT;
    }

    default boolean isTax() {
        return getItemFamilyType() == KnownItemFamily.TAX;
    }

}
