package one.modality.booking.frontoffice.bookingpage.theme;

import javafx.scene.paint.Color;

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

    // === PREDEFINED COLOR SCHEMES ===

    /**
     * Journey Green - Growth & Renewal.
     * Best for: Meditation Retreats, General Programs, Beginner Courses, Nature Centers.
     */
    public static final BookingFormColorScheme JOURNEY_GREEN = new BookingFormColorScheme(
            "journey-green",
            "Journey Green",
            "Growth & Renewal",
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
            Color.web("#1976D2")
    );

    /**
     * Compassion Rose - Love & Kindness.
     * Best for: Compassion Retreats, Heart Practice, Family Events, Loving Kindness.
     */
    public static final BookingFormColorScheme COMPASSION_ROSE = new BookingFormColorScheme(
            "compassion-rose",
            "Compassion Rose",
            "Love & Kindness",
            Color.web("#D81B60")
    );

    /**
     * Peace Purple - Deep Practice.
     * Best for: Silent Retreats, Deep Meditation, Advanced Practice, Tantric Courses.
     */
    public static final BookingFormColorScheme PEACE_PURPLE = new BookingFormColorScheme(
            "peace-purple",
            "Peace Purple",
            "Deep Practice",
            Color.web("#7B1FA2")
    );

    /**
     * Joy Amber - Celebration & Festivals.
     * Best for: Festivals, Celebrations, Community Events, Empowerments.
     */
    public static final BookingFormColorScheme JOY_AMBER = new BookingFormColorScheme(
            "joy-amber",
            "Joy Amber",
            "Celebration & Festivals",
            Color.web("#F57C00")
    );

    /**
     * Calm Teal - Balance & Healing.
     * Best for: Healing Retreats, Mindfulness, Work-Life Balance, Urban Centers.
     */
    public static final BookingFormColorScheme CALM_TEAL = new BookingFormColorScheme(
            "calm-teal",
            "Calm Teal",
            "Balance & Healing",
            Color.web("#00897B")
    );

    /**
     * Clarity Indigo - Focus & Concentration.
     * Best for: Concentration, Analytical Meditation, Night Courses, Evening Programs.
     */
    public static final BookingFormColorScheme CLARITY_INDIGO = new BookingFormColorScheme(
            "clarity-indigo",
            "Clarity Indigo",
            "Focus & Concentration",
            Color.web("#3949AB")
    );

    /**
     * Vajrayogini Red - Power & Transformation.
     * Best for: Vajrayogini Retreats, Heruka Practice, Dorje Shugden Pujas, Highest Yoga Tantra.
     */
    public static final BookingFormColorScheme VAJRAYOGINI_RED = new BookingFormColorScheme(
            "vajrayogini-red",
            "Vajrayogini Red",
            "Power & Transformation",
            Color.web("#D32F2F")
    );

    /**
     * Vajrasattva White - Purification & Clarity.
     * Best for: Vajrasattva Retreats, Purification Practice, Confession Retreats, Preliminary Practices.
     */
    public static final BookingFormColorScheme VAJRASATTVA_WHITE = new BookingFormColorScheme(
            "vajrasattva-white",
            "Vajrasattva White",
            "Purification & Clarity",
            Color.web("#455A64")
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

    private BookingFormColorScheme(String id, String name, String description, Color primary) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.primary = primary;
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

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
