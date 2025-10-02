package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.i18n.EventI18nKeys;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class Timetable {

    private static final double STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH = 400; // mainly to reduce responsive computation on low-end devices
    private static final int MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM = 3;

    private final ObservableList<ScheduledItem> videoScheduledItems; // The list of all videos for that event
    private final ObservableList<ScheduledItem> displayedVideoScheduledItems = FXCollections.observableArrayList(); // The list of the videos to display (all or of selected day)

    private final MonoPane responsiveDaySelectionMonoPane = new MonoPane();

    private final Label selectTheDayBelowLabel = I18nControls.newLabel(VideoStreamingI18nKeys.SelectTheDayBelow);
    private final ObjectProperty<LocalDate> selectedDayProperty = new SimpleObjectProperty<>();
    private final DaySwitcher daySwitcher;

    private boolean displayingDailyProgram;
    private EntityColumn<ScheduledItem>[] dailyProgramVideoColumns;
    private EntityColumn<ScheduledItem>[] allProgramVideoColumns;

    private final VisualGrid videoGrid =
        Screen.getPrimary().getVisualBounds().getWidth() <= STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH ?
            VisualGrid.createVisualGridWithMonoColumnLayoutSkin() :
            VisualGrid.createVisualGridWithResponsiveSkin();

    final AriaToggleGroup<ScheduledItem> watchButtonsGroup = new AriaToggleGroup<>();

    private final HtmlText festivalShopText = Bootstrap.strong(new HtmlText());

    Timetable(ObservableList<ScheduledItem> videoScheduledItems, MonoPane pageContainer, Object activity) {
        this.videoScheduledItems = videoScheduledItems;
        daySwitcher = new DaySwitcher(pageContainer, VideoStreamingI18nKeys.EventSchedule);
        // We bind the currentDate of the daySwitcher to the currentDaySelected so the video appearing are linked to the day selected in the day switcher
        selectedDayProperty.bind(daySwitcher.selectedDateProperty());
        videoGrid.setAppContext(activity); // Passing this VideosActivity as appContext to the value renderers
    }

    void startLogic(EntityStore entityStore) {
        TimetableFormattersAndRenderers.registerRenderers();
        // The columns (and groups) displayed for events with a daily program (such as Festivals)
        dailyProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """      
            [
                {expression: 'this', format: 'videoDate', role: 'group'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: 'this', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), entityStore.getDomainModel(), "ScheduledItem");
        // The columns (and groups) displayed for recurring events with 1 or just a few sessions per day (such as STTP)
        allProgramVideoColumns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'this', format: 'allProgramGroup', textAlign: 'center', styleClass: 'status', role: 'group'},
                {expression: 'this', label: 'Date', format: 'videoDate', hShrink: false, styleClass: 'date'},
                {expression: 'this', label: 'Time', format: 'videoTimeRange', textAlign: 'center', hShrink: false, styleClass: 'time'},
                {expression: 'this', label: '"Session"', renderer: 'videoName', minWidth: 200, styleClass: 'name'},
                {expression: 'this', label: 'Status', renderer: 'videoStatus', textAlign: 'center', hShrink: false, styleClass: 'status'}
            ]""".replace("\"Session\"", EventI18nKeys.Session.toString()), entityStore.getDomainModel(), "ScheduledItem");

    }

    Node buildUi() {
        //The monoPane that manage the program day selection
        new ResponsiveDesign(responsiveDaySelectionMonoPane)
            // 1. Table layout (for desktops)
            .addResponsiveLayout(/* applicability test: */ width -> {
                    //If the grid skin is a table, we're in the desktop mode, otherwise we're in the mobile mode
                    return VisualGrid.isTableLayout(videoGrid);
                }, /* apply method: */ () -> responsiveDaySelectionMonoPane.setContent(daySwitcher.getDesktopView()),
                /* test dependencies: */ videoGrid.skinProperty())
            // 2. Vertical layout (for mobiles)
            .addResponsiveLayout(
                /* apply method: */ () -> responsiveDaySelectionMonoPane.setContent(daySwitcher.getMobileViewContainer())
            ).start();

        Label eventScheduleLabel = Bootstrap.h3(I18nControls.newLabel(VideoStreamingI18nKeys.EventSchedule));
        VBox selectTheDayBelowVBox = new VBox(5, eventScheduleLabel, selectTheDayBelowLabel);
        Layouts.setMinMaxHeightToPref(selectTheDayBelowVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        selectTheDayBelowVBox.setAlignment(Pos.CENTER);
        selectTheDayBelowVBox.setPadding(new Insets(100, 0, 0, 0));

        videoGrid.setMinRowHeight(48);
        videoGrid.setPrefRowHeight(Region.USE_COMPUTED_SIZE);
        videoGrid.setMonoCellMargin(new Insets(5, 10, 5, 10)); // top and bottom are more for mono colum layout (no real effect on table layout)
        videoGrid.setFullHeight(true);
        videoGrid.setHeaderVisible(true);

        I18n.bindI18nTextProperty(festivalShopText.textProperty(), VideoStreamingI18nKeys.FestivalShop);

        return new VBox(5,
            responsiveDaySelectionMonoPane,
            videoGrid, // contains the videos for the selected day (or all days)
            festivalShopText
        );
    }

    void reactToChanges() {
        // Showing selected videos for the selected Day
        FXProperties.runNowAndOnPropertiesChange(() -> {
            LocalDate selectedDay = selectedDayProperty.get();
            if (!displayingDailyProgram || selectedDay == null)
                displayedVideoScheduledItems.setAll(videoScheduledItems);
            else
                displayedVideoScheduledItems.setAll(Collections.filter(videoScheduledItems, item -> selectedDay.equals(item.getDate())));
        }, selectedDayProperty, ObservableLists.versionNumber(videoScheduledItems));

        // Updating the dates in daySwitch after the video sessions have been loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change ->
                daySwitcher.setAvailableDates(videoScheduledItems.stream()
                    .map(ScheduledItem::getDate)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()))
            , videoScheduledItems);

        // Showing selected videos once they are loaded
        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
                VisualResult vr;
                // Because we group video sessions in the table, it's important sessions are sorted by group
                if (displayingDailyProgram) { // daily-program (ex: Festivals) => group = date
                    // Sessions are already correctly sorted by the database query (by ascending date)
                    vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedVideoScheduledItems, dailyProgramVideoColumns);
                } else { // all-program (ex: STTP) => group = LiveNow, Today, Upcoming or Past
                    // Sorting sessions by group, but for Past session, we reverse the chronological order
                    List<ScheduledItem> sortedVideoScheduledItems = displayedVideoScheduledItems.sorted((v1, v2) -> {
                        Object group1 = VideoState.getAllProgramVideoGroupI18nKey(v1); // LiveNow, Today, Upcoming or Past
                        Object group2 = VideoState.getAllProgramVideoGroupI18nKey(v2); // LiveNow, Today, Upcoming or Past
                        // Sorting Past sessions in chronological reverse order (the most recent first)
                        if (BaseI18nKeys.Past.equals(group1) && BaseI18nKeys.Past.equals(group2)) {
                            return -v1.getDate().compareTo(v2.getDate());
                        }
                        // Otherwise sorting by group order (keeping the original sort in groups other than Past - i.e., ascending date)
                        return Integer.compare(VideoState.getAllProgramVideoGroupOrder(group1), VideoState.getAllProgramVideoGroupOrder(group2));
                    });
                    vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(sortedVideoScheduledItems, allProgramVideoColumns);
                }
                videoGrid.setVisualResult(vr);
            }, displayedVideoScheduledItems
            // We also rebuild the whole table on time zone change, because the video date groups may start at different rows for different timezones
            , TimeZoneSwitch.getGlobal().eventLocalTimeSelectedProperty());
    }

    void updateProgramDisplayMode() {
        // We check if we should display the list of the days, so we can select to display the program just for one day.
        displayingDailyProgram = videoScheduledItems.stream()
            .collect(Collectors.groupingBy(ScheduledItem::getDate, Collectors.counting()))
            .values().stream()
            .anyMatch(count -> count > MIN_NUMBER_OF_SESSION_PER_DAY_BEFORE_DISPLAYING_DAILY_PROGRAM);
        Layouts.setManagedAndVisibleProperties(selectTheDayBelowLabel, displayingDailyProgram);
        Layouts.setManagedAndVisibleProperties(responsiveDaySelectionMonoPane, displayingDailyProgram);
        Layouts.setManagedAndVisibleProperties(festivalShopText, displayingDailyProgram);
    }

}
