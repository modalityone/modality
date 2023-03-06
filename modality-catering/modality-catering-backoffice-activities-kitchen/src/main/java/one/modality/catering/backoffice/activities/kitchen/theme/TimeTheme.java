package one.modality.catering.backoffice.activities.kitchen.theme;

import dev.webfx.extras.theme.Facet;
import dev.webfx.extras.theme.Theme;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.luminance.LuminanceFacetCategory;
import dev.webfx.extras.theme.luminance.LuminanceMode;
import dev.webfx.extras.theme.palette.PaletteMode;
import dev.webfx.extras.theme.text.TextFacetCategory;
import dev.webfx.extras.util.colors.ColorSeries;
import dev.webfx.extras.util.colors.Colors;
import dev.webfx.stack.ui.util.background.BackgroundFactory;
import dev.webfx.stack.ui.util.border.BorderFactory;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Bruno Salmon
 */
public class TimeTheme implements Theme {

    // Essential palette mode
    private static final Color ESSENTIAL_MONTH_BACKGROUND_COLOR_UNSELECTED = Color.rgb(131,135,136);
    private static final Color ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED = Color.rgb(0, 150, 214);
    private static final Color ESSENTIAL_MONTH_BACKGROUND_TEXT_COLOR = Color.WHITE;

    private static final Color ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR = Color.rgb( 243, 243, 243); // light gray
    private static final Color ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR_TODAY = Color.rgb( 208, 248, 208); // light green

    // Varied palette mode
    private final static Color[] VARIED_MONTH_BACKGROUND_COLORS_SELECTED;
    private final static Color[] VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED;
    private final static Color[] VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED;
    private static final Color VARIED_MONTH_BACKGROUND_TEXT_COLOR = Color.WHITE;

    private final static Color[] VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS;
    private final static Color[] VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS;
    private static final Color VARIED_DAY_OF_WEEK_TEXT_COLOR = Color.WHITE;
    private static final Color VARIED_DAY_OF_WEEK_BORDER_COLOR = null;

    private final static Color[] VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS;
    private final static Color[] VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS;
    private static final Color VARIED_DATE_PANEL_BACKGROUND_COLOR_TODAY = Color.WHITE;

    static {
        ColorSeries monthColorSeries = Colors.createColorHueShiftSeries(Color.TURQUOISE, 360d / 19);
        monthColorSeries.nextColor();
        VARIED_MONTH_BACKGROUND_COLORS_SELECTED = IntStream.range(0, 12).mapToObj(i -> monthColorSeries.nextColor()).toArray(Color[]::new);
        VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED = Arrays.stream(VARIED_MONTH_BACKGROUND_COLORS_SELECTED).map(c -> Colors.whitenColor(c, 0.5)).toArray(Color[]::new);
        VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED = Arrays.stream(VARIED_MONTH_BACKGROUND_COLORS_SELECTED).map(c -> Colors.blackenColor(c, 0.4)).toArray(Color[]::new);
        ColorSeries weekDayColorSeries = Colors.createColorHueShiftSeries(Colors.whitenColor(Color.rgb(33,129,107), 0.8), 360d / 8);
        VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS = IntStream.range(0, 7).mapToObj(i -> weekDayColorSeries.nextColor()).toArray(Color[]::new);
        VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS = Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS).map(c -> Colors.blackenColor(c, 0.8)).toArray(Color[]::new);
        VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS = Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS).map(c -> Colors.whitenColor(c, 0.2)).toArray(Color[]::new);
        VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS = Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS).map(c -> Colors.blackenColor(c, 0.4)).toArray(Color[]::new);
    }

    public static void register() { } // Can be called several times, but only the first call will trigger the static initializer below

    static {
        ThemeRegistry.registerTheme(new TimeTheme());
    }

    @Override
    public boolean supportsFacetCategory(Object facetCategory) {
        return facetCategory instanceof TimeFacet;
    }

    @Override
    public void styleFacet(Facet facet, Object facetCategory) {
        TimeFacet timeFacet = (TimeFacet) facetCategory;

        Paint textFill = null;
        Color backgroundColor = null;
        Color borderColor = null;

        switch (timeFacet) {

            case YEAR_MONTH_FACET: {
                textFill = getMonthTextColor();
                YearMonth yearMonth = facet.getLogicValue();
                backgroundColor = getMonthBackgroundColor(yearMonth.getMonth(), facet.isSelected());
                break;
            }

            case DAY_OF_WEEK_FACET: {
                ThemeRegistry.styleFacetNow(facet, TextFacetCategory.PRIMARY_TEXT_FACET);
                ThemeRegistry.styleFacetNow(facet, LuminanceFacetCategory.SECONDARY_PANEL_FACET);
                if (PaletteMode.isVariedPalette()) {
                    textFill = getVeriedDayOfWeekTextColor();
                    DayOfWeek dayOfWeek = facet.getLogicValue();
                    backgroundColor = getVariedDayOfWeekBackgroundColor(dayOfWeek);
                    borderColor = getVariedDayOfWeekBorderColor();
                }
                break;
            }

            case DATE_PANEL_FACET: {
                ThemeRegistry.styleFacetNow(facet, LuminanceFacetCategory.SECONDARY_PANEL_FACET);
                if (PaletteMode.isVariedPalette()) {
                    textFill = getMonthTextColor();
                    LocalDate date = facet.getLogicValue();
                    backgroundColor = getDatePanelBackgroundColor(date);
                }
                break;
            }
        }

        Text text = facet.getTextNode();
        Region background = facet.getBackgroundNode();
        if (text != null && textFill != null)
            text.setFill(textFill);
        if (background != null && (backgroundColor != null || borderColor != null)) {
            double radius = 10;
            background.setBackground(backgroundColor == null ? null : BackgroundFactory.newBackground(backgroundColor, radius));
            background.setBorder(borderColor == null ? null : BorderFactory.newBorder(borderColor, radius));
        }
    }

    private static Color getMonthTextColor() {
        return PaletteMode.isVariedPalette() ? // Varied palette mode
                VARIED_MONTH_BACKGROUND_TEXT_COLOR
                // Essential palette mode
                : ESSENTIAL_MONTH_BACKGROUND_TEXT_COLOR;
    }

    private static Color getMonthBackgroundColor(Month month, boolean selected) {
        return PaletteMode.isVariedPalette() ? // Varied palette mode
                (selected ? VARIED_MONTH_BACKGROUND_COLORS_SELECTED : LuminanceMode.isLightMode() ? VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED : VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED)[month.ordinal()]
                // Essential palette mode
                : selected ? ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED : ESSENTIAL_MONTH_BACKGROUND_COLOR_UNSELECTED;
    }

    private static Color getVeriedDayOfWeekTextColor() {
        return VARIED_DAY_OF_WEEK_TEXT_COLOR;
    }

    private static Color getVariedDayOfWeekBackgroundColor(DayOfWeek dayOfWeek) {
        return (LuminanceMode.isLightMode() ? VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS : VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS)[dayOfWeek.ordinal()];
    }

    private static Color getVariedDayOfWeekBorderColor() {
        return VARIED_DAY_OF_WEEK_BORDER_COLOR;
    }

    private static Color getDatePanelBackgroundColor(LocalDate date) {
        boolean isToday = false; //date.equals(LocalDate.now());
        return PaletteMode.isVariedPalette() ? // Varied palette mode
                (isToday ? VARIED_DATE_PANEL_BACKGROUND_COLOR_TODAY : (LuminanceMode.isLightMode() ? VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS : VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS)[date.getDayOfWeek().ordinal()])
                // Essential palette mode
                : isToday ? ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR_TODAY : ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR;
    }

}
