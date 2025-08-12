package one.modality.ecommerce.frontoffice.order;


import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import one.modality.base.client.icons.SvgIcons;

/**
 * @author David Hello
 */
abstract class BaseDialog {

    protected final BorderPane dialogPane = new BorderPane();
    protected static final double DEFAULT_MAX_WIDTH = 500;
    protected static final double DEFAULT_PREF_HEIGHT = 550;

    protected BaseDialog() {
        initializeDialog();
    }

    private void initializeDialog() {
        dialogPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        dialogPane.setMaxWidth(getMaxWidth());
        dialogPane.setPrefHeight(getPrefHeight());
    }

    protected double getMaxWidth() {
        return DEFAULT_MAX_WIDTH;
    }

    protected double getPrefHeight() {
        return DEFAULT_PREF_HEIGHT;
    }

    protected VBox createHeader(Object titleKey, Object subtitleKey) {
        return createHeader(titleKey, subtitleKey, "#2196F3");
    }

    protected VBox createHeader(Object titleKey, Object subtitleKey, String backgroundColor) {
        VBox header = new VBox(5);
        header.setPadding(new Insets(24));
        header.setBackground(new Background(new BackgroundFill(Color.web(backgroundColor), new CornerRadii(12, 12, 0, 0, false), Insets.EMPTY)));

        Label titleLabel = Bootstrap.strong(Bootstrap.h3(I18nControls.newLabel(titleKey)));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        if (subtitleKey != null) {
            Label subtitleLabel = Bootstrap.strong(I18nControls.newLabel(subtitleKey));
            subtitleLabel.setTextFill(Color.WHITE);
            subtitleLabel.setWrapText(true);
            header.getChildren().addAll(titleLabel, subtitleLabel);
        } else {
            header.getChildren().add(titleLabel);
        }

        return header;
    }

    protected VBox createForm() {
        VBox form = new VBox(24);
        form.setPadding(new Insets(32, 24, 24, 24));
        return form;
    }

    protected HBox createButtonGroup(javafx.scene.control.Button... buttons) {
        HBox buttonGroup = new HBox(12, buttons);
        buttonGroup.setAlignment(Pos.CENTER_RIGHT);
        buttonGroup.setPadding(new Insets(0, 24, 24, 24));
        return buttonGroup;
    }

    protected void displaySuccessMessage(Object titleKey, Object messageKey, Object detailsKey, int duration, Runnable onFinished) {
        UiScheduler.runInUiThread(() -> {
            VBox successPane = new VBox(10);
            successPane.setAlignment(Pos.CENTER);
            successPane.setPadding(new Insets(40, 24, 40, 24));

            // Icon
            MonoPane iconLabel = SvgIcons.createSuccessIcon();
            iconLabel.setPadding(new Insets(20, 0, 0, 0));

            // Title
            Label titleLabel = Bootstrap.strong(Bootstrap.h4(I18nControls.newLabel(titleKey)));
            titleLabel.setWrapText(true);
            titleLabel.setTextAlignment(TextAlignment.CENTER);
            titleLabel.setPadding(new Insets(10, 0, 15, 0));

            // Message
            Label messageLabel = I18nControls.newLabel(messageKey);
            messageLabel.setWrapText(true);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.getStyleClass().add("thank-you-message");

            successPane.getChildren().addAll(iconLabel, titleLabel, messageLabel);

            // Details (optional)
            if (detailsKey != null) {
                Label detailsLabel = I18nControls.newLabel(detailsKey);
                detailsLabel.setWrapText(true);
                detailsLabel.setTextAlignment(TextAlignment.CENTER);
                detailsLabel.setPadding(new Insets(30, 0, 0, 0));
                detailsLabel.getStyleClass().add("paiement-row");
                successPane.getChildren().add(detailsLabel);
            }

            // Progress Bar
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.setPrefHeight(4);
            successPane.getChildren().add(progressBar);

            // Replace dialog content
            dialogPane.setCenter(successPane);
            dialogPane.setBottom(null);

            // Animate progress bar
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(new Duration(duration), new KeyValue(progressBar.progressProperty(), 1))
            );
            timeline.setOnFinished(event -> onFinished.run());
            timeline.play();
        });
    }

    public BorderPane getContainer() {
        return dialogPane;
    }

    public abstract void buildUI();
}
