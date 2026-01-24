package one.modality.booking.frontoffice.bookingpage.theme;

import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines color schemes for booking forms. Each event can have its own color scheme
 * based on the type of retreat/course, stored as event.colorScheme in the database.
 *
 * <p>Color scheme theming is now primarily handled via CSS classes (e.g., "theme-wisdom-blue")
 * applied to the root container. This class provides:</p>
 * <ul>
 *   <li>{@link #getId()} - Returns the ID used for CSS theme class names</li>
 *   <li>{@link #getPrimary()} - Returns the primary color for SVG icons (WebFX/GWT requires Java colors)</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public final class BookingFormColorScheme {

    private final String id;
    private final String name;
    private final String description;
    private final Color primary;
    private final Color selectedBg;
    private final Color darkText;
    private final Color hoverBorder;

    // === PREDEFINED COLOR SCHEMES ===

    /**
     * Journey Green - Growth & Renewal.
     * Best for: Meditation Retreats, General Programs, Beginner Courses, Nature Centers.
     */
    public static final BookingFormColorScheme JOURNEY_GREEN = new BookingFormColorScheme(
            "journey-green",
            "Journey Green",
            "Growth & Renewal",
            Color.web("#4CAF50"),  // primary
            Color.web("#E8F5E9"),  // selectedBg
            Color.web("#2E7D32"),  // darkText
            Color.web("#8DD39E")   // hoverBorder
    );

    /**
     * Wisdom Blue - Clarity & Study.
     * Best for: Study Programs, Foundation Program, Philosophy Courses, Teacher Training.
     */
    public static final BookingFormColorScheme WISDOM_BLUE = new BookingFormColorScheme(
            "wisdom-blue",
            "Wisdom Blue",
            "Clarity & Study",
            Color.web("#1976D2"),  // primary
            Color.web("#E3F2FD"),  // selectedBg
            Color.web("#0D47A1"),  // darkText
            Color.web("#90CAF9")   // hoverBorder
    );

    /**
     * Compassion Rose - Love & Kindness.
     * Best for: Compassion Retreats, Heart Practice, Family Events, Loving Kindness.
     */
    public static final BookingFormColorScheme COMPASSION_ROSE = new BookingFormColorScheme(
            "compassion-rose",
            "Compassion Rose",
            "Love & Kindness",
            Color.web("#D81B60"),  // primary
            Color.web("#FCE4EC"),  // selectedBg
            Color.web("#880E4F"),  // darkText
            Color.web("#F48FB1")   // hoverBorder
    );

    /**
     * Peace Purple - Deep Practice.
     * Best for: Silent Retreats, Deep Meditation, Advanced Practice, Tantric Courses.
     */
    public static final BookingFormColorScheme PEACE_PURPLE = new BookingFormColorScheme(
            "peace-purple",
            "Peace Purple",
            "Deep Practice",
            Color.web("#7B1FA2"),  // primary
            Color.web("#F3E5F5"),  // selectedBg
            Color.web("#4A148C"),  // darkText
            Color.web("#CE93D8")   // hoverBorder
    );

    /**
     * Joy Amber - Celebration & Festivals.
     * Best for: Festivals, Celebrations, Community Events, Empowerments.
     */
    public static final BookingFormColorScheme JOY_AMBER = new BookingFormColorScheme(
            "joy-amber",
            "Joy Amber",
            "Celebration & Festivals",
            Color.web("#F57C00"),  // primary
            Color.web("#FFF3E0"),  // selectedBg
            Color.web("#E65100"),  // darkText
            Color.web("#FFB74D")   // hoverBorder
    );

    /**
     * Calm Teal - Balance & Healing.
     * Best for: Healing Retreats, Mindfulness, Work-Life Balance, Urban Centers.
     */
    public static final BookingFormColorScheme CALM_TEAL = new BookingFormColorScheme(
            "calm-teal",
            "Calm Teal",
            "Balance & Healing",
            Color.web("#00897B"),  // primary
            Color.web("#E0F2F1"),  // selectedBg
            Color.web("#004D40"),  // darkText
            Color.web("#80CBC4")   // hoverBorder
    );

    /**
     * Clarity Indigo - Focus & Concentration.
     * Best for: Concentration, Analytical Meditation, Night Courses, Evening Programs.
     */
    public static final BookingFormColorScheme CLARITY_INDIGO = new BookingFormColorScheme(
            "clarity-indigo",
            "Clarity Indigo",
            "Focus & Concentration",
            Color.web("#3949AB"),  // primary
            Color.web("#E8EAF6"),  // selectedBg
            Color.web("#1A237E"),  // darkText
            Color.web("#9FA8DA")   // hoverBorder
    );

    /**
     * Vajrayogini Red - Power & Transformation.
     * Best for: Vajrayogini Retreats, Heruka Practice, Dorje Shugden Pujas, Highest Yoga Tantra.
     */
    public static final BookingFormColorScheme VAJRAYOGINI_RED = new BookingFormColorScheme(
            "vajrayogini-red",
            "Vajrayogini Red",
            "Power & Transformation",
            Color.web("#D32F2F"),  // primary
            Color.web("#FFEBEE"),  // selectedBg
            Color.web("#B71C1C"),  // darkText
            Color.web("#EF9A9A")   // hoverBorder
    );

    /**
     * Vajrasattva White - Purification & Clarity.
     * Best for: Vajrasattva Retreats, Purification Practice, Confession Retreats, Preliminary Practices.
     */
    public static final BookingFormColorScheme VAJRASATTVA_WHITE = new BookingFormColorScheme(
            "vajrasattva-white",
            "Vajrasattva White",
            "Purification & Clarity",
            Color.web("#455A64"),  // primary
            Color.web("#ECEFF1"),  // selectedBg
            Color.web("#263238"),  // darkText
            Color.web("#B0BEC5")   // hoverBorder
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

    /**
     * Returns a color scheme by its ID.
     *
     * @param id The scheme ID (e.g., "wisdom-blue")
     * @return The matching color scheme, or null if not found
     */
    public static BookingFormColorScheme getById(String id) {
        return id != null ? SCHEMES_BY_ID.get(id) : null;
    }

    /**
     * Returns a color scheme by its ID with a fallback.
     *
     * @param id The scheme ID (e.g., "wisdom-blue")
     * @param fallback The fallback scheme if ID is null or not found
     * @return The matching color scheme, or the fallback
     */
    public static BookingFormColorScheme getByIdOrDefault(String id, BookingFormColorScheme fallback) {
        BookingFormColorScheme scheme = getById(id);
        return scheme != null ? scheme : fallback;
    }

    /**
     * Resolves the color scheme from an Event's cssClass field.
     * Falls back to WISDOM_BLUE if the event is null, cssClass is not set,
     * or the cssClass doesn't match any known scheme.
     *
     * @param event The event to get the color scheme from
     * @return The resolved color scheme, or WISDOM_BLUE as fallback
     */
    public static BookingFormColorScheme resolveFromEvent(Event event) {
        if (event == null) {
            return WISDOM_BLUE;
        }
        return getByIdOrDefault(event.getCssClass(), WISDOM_BLUE);
    }

    // === CONSTRUCTOR ===

    private BookingFormColorScheme(String id, String name, String description, Color primary,
                                   Color selectedBg, Color darkText, Color hoverBorder) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.primary = primary;
        this.selectedBg = selectedBg;
        this.darkText = darkText;
        this.hoverBorder = hoverBorder;
    }

    // === GETTERS ===

    /**
     * Returns the unique identifier for this color scheme.
     * Used to apply CSS theme classes (e.g., "theme-wisdom-blue").
     */
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
     * Primary accent color used for SVG icons and dynamic elements.
     * This is needed because WebFX/GWT has unreliable CSS color rendering for SVG strokes/fills.
     */
    public Color getPrimary() {
        return primary;
    }

    /**
     * Light background color for selected/highlighted states (e.g., user badge background).
     */
    public Color getSelectedBg() {
        return selectedBg;
    }

    /**
     * Dark accent text color that complements the primary color.
     */
    public Color getDarkText() {
        return darkText;
    }

    /**
     * Border color used for hover states on interactive elements.
     */
    public Color getHoverBorder() {
        return hoverBorder;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
