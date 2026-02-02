package one.modality.event.frontoffice.medias;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;

import static one.modality.event.frontoffice.medias.MediasCssSelectors.*;

/**
 * View displayed when registration is not yet confirmed.
 * Reusable for both video streaming and audio library.
 */
public class NotConfirmedView {
    private final VBox container;

    public NotConfirmedView() {
        MonoPane noticeIcon = createNoticeIcon();
        Label titleLabel = createTitleLabel(MediasI18nKeys.RegistrationNotConfirmed);
        Label descriptionLabel = createDescriptionLabel(MediasI18nKeys.RegistrationNotConfirmedExplanation);

        container = new VBox(40,
            noticeIcon,
            titleLabel,
            descriptionLabel
        );
        container.setAlignment(Pos.TOP_CENTER);
    }

    private MonoPane createNoticeIcon() {
        MonoPane noticeIcon = new MonoPane();
        noticeIcon.setMinSize(80, 80);
        noticeIcon.setPrefSize(80, 80);
        noticeIcon.setMaxSize(80, 80);
        noticeIcon.getStyleClass().add(notice_icon);

        SVGPath warningPath = SvgIcons.createWarningPath();
        warningPath.setFill(Color.WHITE);
        warningPath.getStyleClass().add(notice_label);

        noticeIcon.setContent(warningPath);
        noticeIcon.setAlignment(Pos.CENTER);

        return noticeIcon;
    }

    private Label createTitleLabel(Object titleI18nKey) {
        Label titleLabel = Bootstrap.h3(I18nControls.newLabel(titleI18nKey));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        return titleLabel;
    }

    private Label createDescriptionLabel(Object descriptionI18nKey) {
        Label descriptionLabel = Bootstrap.textSecondary(I18nControls.newLabel(descriptionI18nKey));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(TextAlignment.CENTER);
        descriptionLabel.setMaxWidth(500);
        return descriptionLabel;
    }

    public Node getView() {
        return container;
    }
}
