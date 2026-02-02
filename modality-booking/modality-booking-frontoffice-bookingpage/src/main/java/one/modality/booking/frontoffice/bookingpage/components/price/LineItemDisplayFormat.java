package one.modality.booking.frontoffice.bookingpage.components.price;

/**
 * Defines how ItemFamily and Item names are displayed together in price line items.
 * Allows flexible formatting based on context and display requirements.
 *
 * @author Claude
 */
public enum LineItemDisplayFormat {

    /**
     * "Accommodation - Single Room" (default)
     * Shows family name, dash separator, then item name.
     * Best for clear hierarchical display.
     */
    FAMILY_DASH_ITEM,

    /**
     * "Single Room"
     * Shows only the item name, hiding the family.
     * Best when family context is already clear from surrounding UI.
     */
    ITEM_ONLY,

    /**
     * "Accommodation"
     * Shows only the family name, hiding the item details.
     * Best for summary views where item specifics are not needed.
     */
    FAMILY_ONLY,

    /**
     * "Single Room (Accommodation)"
     * Shows item name with family in parentheses.
     * Best when item is primary but family context is helpful.
     */
    ITEM_PARENTHESIS_FAMILY
}
