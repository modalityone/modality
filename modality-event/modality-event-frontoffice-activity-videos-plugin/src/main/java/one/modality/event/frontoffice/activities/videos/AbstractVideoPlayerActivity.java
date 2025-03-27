package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
abstract class AbstractVideoPlayerActivity extends ViewDomainActivityBase {

    private static final double IMAGE_HEIGHT = 240;

    protected final Label sessionTitleLabel = Bootstrap.h4(Bootstrap.strong(new Label()));
    protected final Label sessionCommentLabel = new Label();
    protected HtmlText eventDescriptionHtmlText = new HtmlText();
    protected Label eventLabel = Bootstrap.h2(Bootstrap.strong(new Label()));
    private MonoPane imageMonoPane;
    protected VBox playersVBoxContainer;
    protected final ObjectProperty<Object> scheduledVideoItemIdProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<ScheduledItem> scheduledVideoItemProperty = new SimpleObjectProperty<>();
    protected final List<Media> publishedMedias = new ArrayList<>(); // No need to be observable (reacting to scheduledVideoItemProperty is enough)
    protected VBox titleVBox;
    protected VBox pageContainer;
    protected HBox headerHBox;
    protected VBox sessionDescriptionVBox;
    protected ProgressIndicator progressIndicator = Controls.createProgressIndicator(50);

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        headerHBox = new HBox();
        headerHBox.setSpacing(50);
        headerHBox.setPadding(new Insets(0, 20, 0, 20));
        imageMonoPane = new MonoPane();

        headerHBox.getChildren().add(imageMonoPane);
        eventLabel.setWrapText(true);
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.setPadding(new Insets(0, 0, 12, 0));

        eventDescriptionHtmlText.managedProperty().bind(eventDescriptionHtmlText.textProperty().isNotEmpty());
        eventDescriptionHtmlText.setMaxHeight(60);
        titleVBox = new VBox(eventLabel, eventDescriptionHtmlText);

        headerHBox.getChildren().add(titleVBox);


        // Session title
        sessionDescriptionVBox = new VBox(20);
        sessionDescriptionVBox.setAlignment(Pos.TOP_LEFT);
        sessionTitleLabel.setWrapText(true);
        sessionTitleLabel.setTextAlignment(TextAlignment.CENTER);
        sessionCommentLabel.setWrapText(true);

        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        sessionDescriptionVBox.getChildren().addAll(progressIndicator, sessionTitleLabel, sessionCommentLabel);

        playersVBoxContainer = new VBox(30);

        pageContainer = new VBox(40,
            headerHBox,
            sessionDescriptionVBox,
            playersVBoxContainer);

        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.getStyleClass().add("livestream");


        // *************************************************************************************************************
        // ************************************* Building final container **********************************************
        // *************************************************************************************************************
        ScalePane scalePane = new ScalePane(pageContainer);
        scalePane.setVAlignment(VPos.TOP);
        return FOPageUtil.restrictToMaxPageWidthAndApplyPageLeftTopRightBottomPadding(scalePane);
    }

    protected void updateSessionTitleAndVideoPlayerContent() {
        syncHeader();
        syncPlayerContent();
    }

    protected abstract void syncHeader();

    protected void displayProgressIndicator() {
        changeProgressIndicatorState(true);
    }

    protected void hideProgressIndicator() {
        changeProgressIndicatorState(false);
    }

    private void changeProgressIndicatorState(boolean inProgress) {
        if (headerHBox != null)
            headerHBox.setVisible(!inProgress);
        sessionTitleLabel.setVisible(!inProgress);
        sessionCommentLabel.setVisible(!inProgress);
        if (progressIndicator != null)
            Layouts.setManagedAndVisibleProperties(progressIndicator, inProgress);
    }

    protected void updatePicture(Event event) {
        String cloudImagePath = ModalityCloudinary.eventCoverImagePath(event, I18n.getLanguage());
        ModalityCloudinary.loadImage(cloudImagePath, imageMonoPane, -1, IMAGE_HEIGHT, SvgIcons::createVideoIconPath);
    }

    protected abstract void syncPlayerContent();
}
