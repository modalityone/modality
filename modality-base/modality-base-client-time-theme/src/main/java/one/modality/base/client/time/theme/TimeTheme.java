package one.modality.base.client.time.theme;

import static one.modality.base.client.time.theme.TimeFacet.DAY_OF_WEEK_CANVAS_FACET;
import static one.modality.base.client.time.theme.TimeFacet.DAY_OF_WEEK_FACET;

import dev.webfx.extras.theme.*;
import dev.webfx.extras.theme.luminance.FXLuminanceMode;
import dev.webfx.extras.theme.luminance.LuminanceFacetCategory;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.palette.FXPaletteMode;
import dev.webfx.extras.theme.text.TextFacetCategory;
import dev.webfx.extras.util.color.ColorSeries;
import dev.webfx.extras.util.color.Colors;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.stream.IntStream;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
public class TimeTheme implements Theme {

  // Essential palette mode
  private static final Color ESSENTIAL_MONTH_BACKGROUND_COLOR_UNSELECTED = Color.rgb(131, 135, 136);
  private static final Color ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED = Color.rgb(0, 150, 214);
  private static final Color ESSENTIAL_MONTH_BACKGROUND_TEXT_COLOR = Color.WHITE;

  private static final Color ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR =
      Color.rgb(243, 243, 243); // light gray
  private static final Color ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR_TODAY =
      Color.rgb(208, 248, 208); // light green

  // Varied palette mode
  private static final Color[] VARIED_MONTH_BACKGROUND_COLORS_SELECTED;
  private static final Color[] VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED;
  private static final Color[] VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED;
  private static final Color VARIED_MONTH_BACKGROUND_TEXT_COLOR = Color.WHITE;

  private static final Color[] VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS;
  private static final Color[] VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS;
  private static final Color VARIED_DAY_OF_WEEK_TEXT_COLOR = Color.WHITE;
  private static final Color VARIED_DAY_OF_WEEK_BORDER_COLOR = null;

  private static final Color[] VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS;
  private static final Color[] VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS;
  private static final Color VARIED_DATE_PANEL_BACKGROUND_COLOR_TODAY = Color.WHITE;

  static {
    ColorSeries monthColorSeries = Colors.createColorHueShiftSeries(Color.TURQUOISE, 360d / 19);
    monthColorSeries.nextColor();
    VARIED_MONTH_BACKGROUND_COLORS_SELECTED =
        IntStream.range(0, 12).mapToObj(i -> monthColorSeries.nextColor()).toArray(Color[]::new);
    VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED =
        Arrays.stream(VARIED_MONTH_BACKGROUND_COLORS_SELECTED)
            .map(c -> Colors.whitenColor(c, 0.5))
            .toArray(Color[]::new);
    VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED =
        Arrays.stream(VARIED_MONTH_BACKGROUND_COLORS_SELECTED)
            .map(c -> Colors.blackenColor(c, 0.4))
            .toArray(Color[]::new);
    ColorSeries weekDayColorSeries =
        Colors.createColorHueShiftSeries(
            Colors.whitenColor(Color.rgb(33, 129, 107), 0.8), 360d / 8);
    VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS =
        IntStream.range(0, 7).mapToObj(i -> weekDayColorSeries.nextColor()).toArray(Color[]::new);
    VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS =
        Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS)
            .map(c -> Colors.blackenColor(c, 0.8))
            .toArray(Color[]::new);
    VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS =
        Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS)
            .map(c -> Colors.whitenColor(c, 0.2))
            .toArray(Color[]::new);
    VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS =
        Arrays.stream(VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS)
            .map(c -> Colors.blackenColor(c, 0.4))
            .toArray(Color[]::new);
  }

  private static final TimeTheme TIME_THEME = new TimeTheme();

  public static void
      register() {} // Can be called several times, but only the first call will trigger the static
                    // initializer below

  static {
    ThemeRegistry.registerTheme(TIME_THEME);
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
      case MONTH_FACET:
        {
          textFill = getMonthTextColor();
          Object logicValue = facet.getLogicValue();
          if (logicValue instanceof YearMonth)
            backgroundColor = getMonthBackgroundColor((YearMonth) logicValue, facet.isSelected());
          else if (logicValue instanceof Month)
            backgroundColor = getMonthBackgroundColor((Month) logicValue, facet.isSelected());
          borderColor = getMonthBorderColor();
          break;
        }

      case DAY_OF_WEEK_FACET:
        {
          ThemeRegistry.styleFacetNow(facet, TextFacetCategory.PRIMARY_TEXT_FACET);
          ThemeRegistry.styleFacetNow(facet, LuminanceFacetCategory.SECONDARY_PANEL_FACET);
          if (facet.isSelected()) {
            backgroundColor = ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED;
            textFill = Color.WHITE;
          } else if (FXPaletteMode.isVariedPalette()) {
            textFill = getVariedDayOfWeekTextColor();
            Object logicValue = facet.getLogicValue();
            if (logicValue instanceof LocalDate)
              logicValue = ((LocalDate) logicValue).getDayOfWeek();
            backgroundColor = getVariedDayOfWeekBackgroundColor((DayOfWeek) logicValue);
            borderColor = getVariedDayOfWeekBorderColor();
          }
          break;
        }

      case DATE_PANEL_FACET:
        {
          ThemeRegistry.styleFacetNow(facet, LuminanceFacetCategory.SECONDARY_PANEL_FACET);
          if (FXPaletteMode.isVariedPalette()) {
            textFill = getMonthTextColor();
            backgroundColor = getDatePanelBackgroundColor((Object) facet.getLogicValue());
          }
          break;
        }

      case DAY_OF_WEEK_CANVAS_FACET:
        {
          ThemeRegistry.styleFacetNow(facet, LuminanceFacetCategory.PRIMARY_PANEL_FACET);
          if (facet.isSelected()) {
            backgroundColor = ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED;
            textFill = Color.WHITE;
          } else if (FXPaletteMode.isVariedPalette()) {
            textFill = getMonthTextColor();
            backgroundColor = getDatePanelBackgroundColor((Object) facet.getLogicValue());
          }
          break;
        }
    }

    Text text = facet.getTextNode();
    Region backgroundRegion = facet.getBackgroundNode();
    if (text != null && textFill != null) ThemeUtil.applyTextFill(text, textFill);
    if (backgroundRegion != null && (backgroundColor != null || borderColor != null)) {
      double radius = 10;
      ThemeUtil.applyBackground(backgroundRegion, backgroundColor, radius);
      ThemeUtil.applyBorder(backgroundRegion, borderColor, radius);
    }
  }

  // =================================================================================================================
  // === Public methods called by styleFacet() that can also be called externally (typically for
  // canvas operation) ===
  // =================================================================================================================
  // -> because they return a value that works for all cases
  // =================================================================================================================

  public static Color getMonthTextColor() {
    return FXPaletteMode.isVariedPalette()
        ? // Varied palette mode
        VARIED_MONTH_BACKGROUND_TEXT_COLOR
        // Essential palette mode
        : ESSENTIAL_MONTH_BACKGROUND_TEXT_COLOR;
  }

  public static Color getMonthBorderColor() {
    return Color.TRANSPARENT;
  }

  public static Color getMonthBackgroundColor(YearMonth yearMonth, boolean selected) {
    return getMonthBackgroundColor(yearMonth.getMonth(), selected);
  }

  public static Color getMonthBackgroundColor(Month month, boolean selected) {
    return FXPaletteMode.isVariedPalette()
        ? // Varied palette mode
        (selected
                ? VARIED_MONTH_BACKGROUND_COLORS_SELECTED
                : FXLuminanceMode.isLightMode()
                    ? VARIED_LIGHT_MONTH_BACKGROUND_COLORS_UNSELECTED
                    : VARIED_DARK_MONTH_BACKGROUND_COLORS_UNSELECTED)
            [month.ordinal()]
        // Essential palette mode
        : selected
            ? ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED
            : ESSENTIAL_MONTH_BACKGROUND_COLOR_UNSELECTED;
  }

  // =================================================================================================================
  // ===                Private methods called by styleFacet() that can't be called externally
  //               ===
  // =================================================================================================================
  // -> because they return a value that works only in specific cases
  // =================================================================================================================

  private static Color getVariedDayOfWeekTextColor() {
    return VARIED_DAY_OF_WEEK_TEXT_COLOR;
  }

  private static Color getVariedDayOfWeekBackgroundColor(DayOfWeek dayOfWeek) {
    return (FXLuminanceMode.isLightMode()
            ? VARIED_LIGHT_DAY_OF_WEEK_BACKGROUND_COLORS
            : VARIED_DARK_DAY_OF_WEEK_BACKGROUND_COLORS)
        [dayOfWeek.ordinal()];
  }

  private static Color getVariedDayOfWeekBorderColor() {
    return VARIED_DAY_OF_WEEK_BORDER_COLOR;
  }

  private static Color getDatePanelBackgroundColor(Object logicalValue) {
    if (logicalValue instanceof DayOfWeek)
      return getDatePanelBackgroundColor((DayOfWeek) logicalValue);
    return getDatePanelBackgroundColor((LocalDate) logicalValue);
  }

  private static Color getDatePanelBackgroundColor(DayOfWeek dayOfWeek) {
    return getDatePanelBackgroundColor(dayOfWeek, false);
  }

  private static Color getDatePanelBackgroundColor(LocalDate date) {
    return getDatePanelBackgroundColor(
        date == null ? DayOfWeek.MONDAY : date.getDayOfWeek(),
        false /* date.equals(LocalDate.now() */);
  }

  private static Color getDatePanelBackgroundColor(DayOfWeek dayOfWeek, boolean isToday) {
    return FXPaletteMode.isVariedPalette()
        ? // Varied palette mode
        (isToday
            ? VARIED_DATE_PANEL_BACKGROUND_COLOR_TODAY
            : (FXLuminanceMode.isLightMode()
                    ? VARIED_LIGHT_DATE_PANEL_BACKGROUND_COLORS
                    : VARIED_DARK_DATE_PANEL_BACKGROUND_COLORS)
                [dayOfWeek.ordinal()])
        // Essential palette mode
        : isToday
            ? ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR_TODAY
            : ESSENTIAL_DATE_PANEL_BACKGROUND_COLOR;
  }

  // =================================================================================================================
  // ===         Public methods designed to be called externally only (typically for canvas
  // operation)             ===
  // =================================================================================================================
  // -> because these methods are requesting a style information that depends on other themes (this
  // is why styleFacet()
  // makes calls to ThemeRegistry.styleFacetNow() in these cases before adding its proper style on
  // top of that).
  // So to reply to these external calls, these methods actually call styleFacet() with a generic
  // facet, capture the
  // style, and return the requested value from the captures style.
  // =================================================================================================================

  // Canvas background

  public static Paint getCanvasBackgroundColor() {
    return FXLuminanceMode.isLightMode()
        ? Color.ANTIQUEWHITE
        : LuminanceTheme.getSecondaryBackgroundColor(false);
  }

  // Year properties

  public static Paint getYearBackgroundColor(boolean selected) {
    return getDayOfWeekBackgroundColor(DayOfWeek.MONDAY, selected);
  }

  public static Paint getYearBorderColor() {
    return Color.TRANSPARENT;
  }

  public static Paint getYearTextColor(boolean selected) {
    return getDayOfWeekTextColor(selected);
  }

  // Month properties

  // Week properties

  public static Paint getWeekBackgroundColor(boolean selected) {
    return selected
        ? ESSENTIAL_MONTH_BACKGROUND_COLOR_SELECTED
        : getDayOfWeekBackgroundColor(DayOfWeek.MONDAY, false);
  }

  public static Paint getWeekBorderColor() {
    return getMonthBorderColor();
  }

  public static Paint getWeekTextColor(boolean selected) {
    return getDayOfWeekTextColor(selected);
  }

  // DayOfWeek properties

  public static Paint getDayOfWeekTextColor(boolean selected) {
    return StyleCapture.captureStyle(TIME_THEME, DayOfWeek.MONDAY, selected, DAY_OF_WEEK_FACET)
        .getTextFill();
  }

  public static Paint getDayOfWeekBackgroundColor(LocalDate date, boolean selected) {
    return getDayOfWeekBackgroundColor(date.getDayOfWeek(), selected);
  }

  public static Paint getDayOfWeekBackgroundColor(DayOfWeek dayOfWeek, boolean selected) {
    return StyleCapture.captureStyle(TIME_THEME, dayOfWeek, selected, DAY_OF_WEEK_FACET)
        .getBackgroundFill();
  }

  public static Paint getDayOfWeekCanvasBackgroundColor(LocalDate date) {
    return getDayOfWeekCanvasBackgroundColor((Object) date);
  }

  public static Paint getDayOfWeekCanvasBackgroundColor(DayOfWeek dayOfWeek) {
    return getDayOfWeekCanvasBackgroundColor((Object) dayOfWeek);
  }

  public static Paint getDayOfWeekCanvasBackgroundColor(Object logicalValue) {
    return getDayOfWeekCanvasBackgroundColor(logicalValue, false);
  }

  public static Paint getDayOfWeekCanvasBackgroundColor(Object logicalValue, boolean selected) {
    return StyleCapture.captureStyle(TIME_THEME, logicalValue, selected, DAY_OF_WEEK_CANVAS_FACET)
        .getBackgroundFill();
  }

  public static Paint getDayOfWeekBorderColor() {
    return getDayOfWeekBorderColor(false);
  }

  public static Paint getDayOfWeekBorderColor(boolean selected) {
    return StyleCapture.captureStyle(TIME_THEME, DayOfWeek.MONDAY, selected, DAY_OF_WEEK_FACET)
        .getBorderFill();
  }
}
