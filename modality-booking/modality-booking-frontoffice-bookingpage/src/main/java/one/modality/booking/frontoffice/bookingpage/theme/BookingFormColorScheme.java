package one.modality.booking.frontoffice.bookingpage.theme;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines color schemes for booking forms. Each event can have its own color scheme
 * based on the type of retreat/course, stored as event.colorScheme in the database.
 * All schemes maintain WCAG AAA accessibility.
 *
 * @author Bruno Salmon
 */
public final class BookingFormColorScheme {

    private final String id;
    private final String name;
    private final String description;
    private final Color primary;
    private final Color selectedBg;
    private final Color hoverBorder;
    private final Color darkText;
    private final Color lightText;

    // === PREDEFINED COLOR SCHEMES ===

    /**
     * Journey Green - Growth & Renewal.
     * Best for: Meditation Retreats, General Programs, Beginner Courses, Nature Centers.
     */
    public static final BookingFormColorScheme JOURNEY_GREEN = new BookingFormColorScheme(
            "journey-green",
            "Journey Green",
            "Growth & Renewal",
            Color.web("#4CAF50"),
            Color.web("#E8F5E9"),
            Color.web("#8DD39E"),
            Color.web("#2E7D32"),
            Color.web("#4CAF50")
    );

    /**
     * Wisdom Blue - Clarity & Study.
     * Best for: Study Programs, Foundation Program, Philosophy Courses, Teacher Training.
     */
    public static final BookingFormColorScheme WISDOM_BLUE = new BookingFormColorScheme(
            "wisdom-blue",
            "Wisdom Blue",
            "Clarity & Study",
            Color.web("#1976D2"),
            Color.web("#E3F2FD"),
            Color.web("#90CAF9"),
            Color.web("#0D47A1"),
            Color.web("#42A5F5")
    );

    /**
     * Compassion Rose - Love & Kindness.
     * Best for: Compassion Retreats, Heart Practice, Family Events, Loving Kindness.
     */
    public static final BookingFormColorScheme COMPASSION_ROSE = new BookingFormColorScheme(
            "compassion-rose",
            "Compassion Rose",
            "Love & Kindness",
            Color.web("#D81B60"),
            Color.web("#FCE4EC"),
            Color.web("#F48FB1"),
            Color.web("#880E4F"),
            Color.web("#EC407A")
    );

    /**
     * Peace Purple - Deep Practice.
     * Best for: Silent Retreats, Deep Meditation, Advanced Practice, Tantric Courses.
     */
    public static final BookingFormColorScheme PEACE_PURPLE = new BookingFormColorScheme(
            "peace-purple",
            "Peace Purple",
            "Deep Practice",
            Color.web("#7B1FA2"),
            Color.web("#F3E5F5"),
            Color.web("#CE93D8"),
            Color.web("#4A148C"),
            Color.web("#9C27B0")
    );

    /**
     * Joy Amber - Celebration & Festivals.
     * Best for: Festivals, Celebrations, Community Events, Empowerments.
     */
    public static final BookingFormColorScheme JOY_AMBER = new BookingFormColorScheme(
            "joy-amber",
            "Joy Amber",
            "Celebration & Festivals",
            Color.web("#F57C00"),
            Color.web("#FFF3E0"),
            Color.web("#FFB74D"),
            Color.web("#E65100"),
            Color.web("#FF9800")
    );

    /**
     * Calm Teal - Balance & Healing.
     * Best for: Healing Retreats, Mindfulness, Work-Life Balance, Urban Centers.
     */
    public static final BookingFormColorScheme CALM_TEAL = new BookingFormColorScheme(
            "calm-teal",
            "Calm Teal",
            "Balance & Healing",
            Color.web("#00897B"),
            Color.web("#E0F2F1"),
            Color.web("#80CBC4"),
            Color.web("#004D40"),
            Color.web("#26A69A")
    );

    /**
     * Clarity Indigo - Focus & Concentration.
     * Best for: Concentration, Analytical Meditation, Night Courses, Evening Programs.
     */
    public static final BookingFormColorScheme CLARITY_INDIGO = new BookingFormColorScheme(
            "clarity-indigo",
            "Clarity Indigo",
            "Focus & Concentration",
            Color.web("#3949AB"),
            Color.web("#E8EAF6"),
            Color.web("#9FA8DA"),
            Color.web("#1A237E"),
            Color.web("#5C6BC0")
    );

    /**
     * Vajrayogini Red - Power & Transformation.
     * Best for: Vajrayogini Retreats, Heruka Practice, Dorje Shugden Pujas, Highest Yoga Tantra.
     */
    public static final BookingFormColorScheme VAJRAYOGINI_RED = new BookingFormColorScheme(
            "vajrayogini-red",
            "Vajrayogini Red",
            "Power & Transformation",
            Color.web("#D32F2F"),
            Color.web("#FFEBEE"),
            Color.web("#EF5350"),
            Color.web("#B71C1C"),
            Color.web("#E57373")
    );

    /**
     * Vajrasattva White - Purification & Clarity.
     * Best for: Vajrasattva Retreats, Purification Practice, Confession Retreats, Preliminary Practices.
     */
    public static final BookingFormColorScheme VAJRASATTVA_WHITE = new BookingFormColorScheme(
            "vajrasattva-white",
            "Vajrasattva White",
            "Purification & Clarity",
            Color.web("#455A64"),
            Color.web("#FAFAFA"),
            Color.web("#78909C"),
            Color.web("#263238"),
            Color.web("#90A4AE")
    );

    /**
     * Default color scheme (Journey Green).
     */
    public static final BookingFormColorScheme DEFAULT = JOURNEY_GREEN;

    // === LOOKUP MAP ===

    private static final Map<String, BookingFormColorScheme> SCHEMES_BY_ID = new HashMap<>();

    static {
        register(JOURNEY_GREEN);
        register(WISDOM_BLUE);
        register(COMPASSION_ROSE);
        register(PEACE_PURPLE);
        register(JOY_AMBER);
        register(CALM_TEAL);
        register(CLARITY_INDIGO);
        register(VAJRAYOGINI_RED);
        register(VAJRASATTVA_WHITE);
    }

    private static void register(BookingFormColorScheme scheme) {
        SCHEMES_BY_ID.put(scheme.getId(), scheme);
    }

    /**
     * Returns all available color schemes.
     *
     * @return Iterable of all registered color schemes
     */
    public static Iterable<BookingFormColorScheme> getAllSchemes() {
        return SCHEMES_BY_ID.values();
    }

    // === CONSTRUCTOR ===

    private BookingFormColorScheme(String id, String name, String description,
                                   Color primary, Color selectedBg, Color hoverBorder,
                                   Color darkText, Color lightText) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.primary = primary;
        this.selectedBg = selectedBg;
        this.hoverBorder = hoverBorder;
        this.darkText = darkText;
        this.lightText = lightText;
    }

    // === GETTERS ===

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Primary accent color used for main interactive elements.
     */
    public Color getPrimary() {
        return primary;
    }

    /**
     * Background color for selected/active states.
     */
    public Color getSelectedBg() {
        return selectedBg;
    }

    /**
     * Border color on hover states.
     */
    public Color getHoverBorder() {
        return hoverBorder;
    }

    /**
     * Dark text color for headings and important text.
     */
    public Color getDarkText() {
        return darkText;
    }

    /**
     * Lighter text color for secondary text.
     */
    public Color getLightText() {
        return lightText;
    }

    // === UTILITY METHODS ===

    /**
     * Converts a Color to hex string (e.g., "#4CAF50").
     */
    public static String toHex(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "#" + toHexByte(r) + toHexByte(g) + toHexByte(b);
    }

    /**
     * Converts an integer (0-255) to a two-character hex string (GWT-compatible).
     */
    private static String toHexByte(int value) {
        String hex = Integer.toHexString(value).toUpperCase();
        return hex.length() == 1 ? "0" + hex : hex;
    }

    /**
     * Returns a darker version of the primary color for hover states.
     * This is calculated by reducing the brightness by 15%.
     */
    public Color getPrimaryDarker() {
        return primary.darker();
    }

    /**
     * Returns the darker primary color as a hex string.
     */
    public String getPrimaryDarkerHex() {
        return toHex(getPrimaryDarker());
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
