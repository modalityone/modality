package one.modality.event.frontoffice.activities.booking.browser;

import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.routing.uirouter.activity.view.ViewActivityContext;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityBase;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.dialog.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;

/**
 * @author Bruno Salmon
 */
public final class BrowserUtil {

    private static final String INTERNAL_BROWSER_ROUTE = "/website";
    private static UiRouter uiRouter;
    private static final WebView internalBrowser = new WebView();

    public static void openExternalBrowser(String url) {
        // Following code is commented because HostServices is not working on Gluon mobiles.
        //WebFxKitLauncher.getApplication().getHostServices().showDocument(url);
        // So we use the Browser service API instead (which as a Gluon implementation)
        try {
            Browser.launchExternalBrowser(url);
        } catch (Exception e) {
            Console.log(e);
        }
    }

    public static void setUiRouter(UiRouter uiRouter) {
        BrowserUtil.uiRouter = uiRouter;
        // Registering the website route in the ui router
        uiRouter.route(INTERNAL_BROWSER_ROUTE, () -> new ViewActivityBase<ViewActivityContextFinal>() {
            @Override
            public Node buildUi() {
                return internalBrowser;
            }
        }, ViewActivityContext::create);
    }

    public static void openInternalBrowser(String url) {
        // Loading the url in the internal browser
        internalBrowser.getEngine().load(url);
        // Going to the route to display that browser
        uiRouter.getHistory().push(INTERNAL_BROWSER_ROUTE);
    }

    public static void chooseHowToOpenWebsite(String url) {
        Hyperlink insideAppLink = GeneralUtility.createHyperlink("openInsideApp", Color.WHITE, 21);
        Hyperlink outsideAppLink = GeneralUtility.createHyperlink("openOutsideApp", Color.WHITE, 21);
        Hyperlink copyLink = GeneralUtility.createHyperlink("copyLink", Color.WHITE, 21);
        VBox vBox = new VBox(30, insideAppLink, outsideAppLink, copyLink);
        vBox.setBorder(Border.stroke(Color.WHITE));
        vBox.setBackground(Background.fill(StyleUtility.MAIN_BLUE_COLOR));
        vBox.setAlignment(Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(vBox, FXMainFrameDialogArea.getDialogArea());
        vBox.setPadding(new Insets(50));
        insideAppLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            openInternalBrowser(url);
        });
        outsideAppLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            openExternalBrowser(url);
        });
        copyLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            ClipboardContent content = new ClipboardContent();
            content.putString(url);
            Clipboard.getSystemClipboard().setContent(content);
        });
        vBox.requestLayout();
        SceneUtil.runOnceFocusIsOutside(vBox, true, dialogCallback::closeDialog);
    }
}
