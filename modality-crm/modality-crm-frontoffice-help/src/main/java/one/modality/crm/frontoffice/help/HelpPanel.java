package one.modality.crm.frontoffice.help;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.icons.SvgIcons;

/**
 * @author Bruno Salmon
 */
public final class HelpPanel {

    public static Node createHelpPanel(Object helpI18nKey, Object helpEmail) {
        SVGPath headPhoneSvgPath = SvgIcons.createHeadphonesPath();
        MonoPane headPhoneMonoPane = new MonoPane(headPhoneSvgPath);

        Label needHelp = Bootstrap.h3(Bootstrap.textSecondary(I18nControls.newLabel(helpI18nKey)));
        needHelp.setTextAlignment(TextAlignment.CENTER);
        needHelp.setWrapText(true);

        // Using an HtmlText rather than a label so it can contain a clickable link for the email.
        HtmlText emailUs = Bootstrap.strong(new HtmlText());
        I18n.bindI18nTextProperty(emailUs.textProperty(), HelpI18nKeys.EmailUs1, helpEmail);

        VBox vBox = new VBox(30, headPhoneMonoPane, needHelp, emailUs);
        vBox.setPadding(new Insets(100, 0, 50, 0));
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

}
