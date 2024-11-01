package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.CenteredPane;
import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.Strings;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import javafx.application.Platform;
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
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.activity.FrontOfficeActivityUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class EventAudioPlaylistActivity extends ViewDomainActivityBase {

    private static final double PAGE_TOP_BOTTOM_PADDING = 100;

    private final ObjectProperty<Object> pathEventIdProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<ScheduledItem> scheduledAudioItems = FXCollections.observableArrayList();
    private final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledAudioItems is enough)

    @Override
    protected void updateModelFromContextParameters() {
        pathEventIdProperty.set(Numbers.toInteger(getParameter(EventAudioPlaylistRouting.PATH_EVENT_ID_PARAMETER_NAME)));
    }

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        EntityStore entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the required data in dependence of the request event and the user account
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object eventId = pathEventIdProperty.get();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (eventId == null || userPersonId == null) { // No access if the user is not logged in or registered
                publishedMedias.clear();
                scheduledAudioItems.clear(); // will trigger UI update
                eventProperty.set(null); // will update i18n bindings
            } else {
                entityStore.executeQueryBatch(
                        new EntityStoreQuery("select name, label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate, livestreamUrl, vodExpirationDate" +
                                             " from Event" +
                                             " where id=? limit 1",
                            new Object[]{eventId}),
                        new EntityStoreQuery("select date, parent.(name, timeline.(startTime, endTime)), event" +
                                             " from ScheduledItem si" +
                                             " where event=? and item.family.code=? and exists(select Attendance where scheduledItem=si and documentLine.(!cancelled and document.(person=? and price_balance<=0)))" +
                                             " order by date",
                            new Object[]{eventId, KnownItemFamily.AUDIO_RECORDING.getCode(), userPersonId}),
                        new EntityStoreQuery("select url, scheduledItem.(date, event), published, durationMillis" +
                                             " from Media" +
                                             " where scheduledItem.(event=? and item.family.code=? and online) and published",
                            new Object[]{eventId, KnownItemFamily.AUDIO_RECORDING.getCode()}))
                    .onFailure(Console::log)
                    .onSuccess(entityLists -> Platform.runLater(() -> {
                        Collections.setAll(publishedMedias, entityLists[2]);
                        scheduledAudioItems.setAll(entityLists[1]); // will trigger UI update
                        eventProperty.set((Event) Collections.first(entityLists[0])); // will update i18n bindings
                    }));
            }
        }, pathEventIdProperty, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        MonoPane backArrow = SvgIcons.createButtonPane(SvgIcons.createBackArrow(), getHistory()::goBack);

        Label eventLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.newLabel(new I18nSubKey("expression: i18n(this)", eventProperty), eventProperty)));
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);

        Label eventDescriptionLabel = I18nControls.newLabel(new I18nSubKey("expression: shortDescription", eventProperty), eventProperty);
        eventDescriptionLabel.setWrapText(true);
        eventDescriptionLabel.setTextAlignment(TextAlignment.CENTER);
        eventDescriptionLabel.managedProperty().bind(FXProperties.compute(eventDescriptionLabel.textProperty(), Strings::isNotEmpty));

        VBox titleVBox = new VBox(eventLabel, eventDescriptionLabel);

        CenteredPane backArrowAndTitlePane = new CenteredPane();
        backArrowAndTitlePane.setLeft(backArrow);
        backArrowAndTitlePane.setCenter(titleVBox);

        VBox audioTracksVBox = new VBox(10);
        audioTracksVBox.setMaxWidth(Region.USE_PREF_SIZE);

        Label noContentLabel = Bootstrap.h3(Bootstrap.textWarning(I18nControls.newLabel(AudioRecordingsI18nKeys.NoAudioRecordingForThisEvent)));
        noContentLabel.setPadding(new Insets(150, 0, 100, 0));

        VBox loadedContentVBox = new VBox(100,
            backArrowAndTitlePane,
            audioTracksVBox
        );
        loadedContentVBox.setAlignment(Pos.CENTER);

        Node loadingContentIndicator = new GoldenRatioPane(ControlUtil.createProgressIndicator(100));

        MonoPane pageContainer = new MonoPane();


        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        ObservableLists.runNowAndOnListOrPropertiesChange(change -> {
            // We display the loading indicator while the data is loading
            if (eventProperty.get() == null) { // this indicates that the data has not finished loaded
                pageContainer.setContent(loadingContentIndicator);
                // TODO display something else (ex: next online events to book) when the user is not logged in, or registered
            } else { // otherwise we display loadedContentVBox and set the content of audioTracksVBox
                pageContainer.setContent(loadedContentVBox);
                // Does this event have audio recordings, and did the person booked and paid for them?
                if (!scheduledAudioItems.isEmpty()) { // yes => we show them as a list of playable tracks
                    audioTracksVBox.getChildren().setAll(Collections.map(scheduledAudioItems, scheduledRecordingItem ->
                        new SessionAudioTrackView(scheduledRecordingItem, publishedMedias).getView())
                    );
                } else { // no => we indicate that there is no recordings for that event
                    audioTracksVBox.getChildren().setAll(noContentLabel);
                    // TODO display another message when the event actually has audio recordings, but the user didn't book them
                }
            }
        }, scheduledAudioItems, eventProperty);


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************

        // Setting a max width for big desktop screens
        pageContainer.setPadding(new Insets(PAGE_TOP_BOTTOM_PADDING, 0, PAGE_TOP_BOTTOM_PADDING, 0));
        return FrontOfficeActivityUtil.createActivityPageScrollPane(pageContainer, false);
    }

}
