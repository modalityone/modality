package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.KnownItemFamily;
import one.modality.crm.shared.services.authn.fx.FXUserPersonId;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

final class AudioRecordingsActivity extends ViewDomainActivityBase {

    private EntityStore entityStore;
    // Holding an observable list of events with audio recordings booked by the user (changes on login & logout)
    private final ObservableList<Event> eventsWithBookedAudios = FXCollections.observableArrayList();

    private final VBox mainVBox = new VBox();
    private final TransitionPane transitionPane = new TransitionPane();

    @Override
    protected void startLogic() {
        // Creating our own entity store to hold the loaded data without interfering with other activities
        entityStore = EntityStore.create(getDataSourceModel()); // Activity datasource model is available at this point
        // Loading the list of events with videos booked by the user and put it into bookedVideoEvents
        FXProperties.runNowAndOnPropertiesChange(() -> {
            eventsWithBookedAudios.clear();
            EntityId userPersonId = FXUserPersonId.getUserPersonId();
            if (userPersonId != null) {
                entityStore.<Event>executeQuery(
                    "select name,label.(de,en,es,fr,pt), shortDescription, audioExpirationDate, startDate, endDate" +
                    " from Event e where exists(select DocumentLine where document.(event = e and person=? and price_balance<=0) and item.family.code=?)" +
                    " order by startDate desc",
                        new Object[]{ userPersonId, KnownItemFamily.AUDIO_RECORDING.getCode() })
                    .onFailure(Console::log)
                    .onSuccess(events -> Platform.runLater(() -> eventsWithBookedAudios.setAll(events)));
            }
        }, FXUserPersonId.userPersonIdProperty());
    }

    @Override
    public Node buildUi() {
        Label titleLabel = Bootstrap.h2(Bootstrap.strong(I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.AudioRecordingTitle)));
        Label titleDescriptionLabel = I18nControls.bindI18nProperties(new Label(), AudioRecordingsI18nKeys.AudioRecordingTitleDescription);
        VBox yearEventVBox = new VBox();

        mainVBox.getChildren().setAll(
            titleLabel,
            titleDescriptionLabel,
            yearEventVBox
        );

        ObservableLists.runNowAndOnListChange(change -> {
            TreeMap<Integer, List<Event>> perYearEvents = new TreeMap<>(Comparator.reverseOrder());
            perYearEvents.putAll(eventsWithBookedAudios.stream().collect(Collectors.groupingBy(e -> e.getStartDate().getYear())));
            yearEventVBox.getChildren().clear();
            perYearEvents.forEach((year, events) -> yearEventVBox.getChildren().add(
                new EventsOfYearView(year, events, this::showRecordingsForEvent).getView()
            ));
        }, eventsWithBookedAudios);

        transitionPane.setPadding(new Insets(100));
        transitionPane.setTransition(new CircleTransition());
        transitionPane.setScrollToTop(true);
        transitionPane.transitToContent(mainVBox);

        return ControlUtil.createVerticalScrollPane(transitionPane);
    }

    private void showRecordingsForEvent(Event event) {
        transitionPane.transitToContent(
            new RecordingsOfEventView(event, () -> transitionPane.transitToContent(mainVBox)).getView()
        );
    }

}
