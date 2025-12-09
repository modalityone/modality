package one.modality.base.frontoffice.utility.browser;

import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.scene.SceneUtil;
import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.routing.uirouter.activity.view.ViewActivityContext;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityBase;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
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
import one.modality.base.client.brand.Brand;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.frontoffice.mainframe.fx.FXBackgroundNode;
import one.modality.base.frontoffice.mainframe.fx.FXCollapseMenu;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Bruno Salmon
 */
public final class BrowserUtil {

    private static UiRouter uiRouter;
    private static final WebView internalBrowser = new WebView();
    private static final Set<String> REGISTERED_ROUTES = new HashSet<>();

    static {
        internalBrowser.setMaxHeight(Double.MAX_VALUE);
    }

    public static void openExternalBrowser(String url) {
        // The following code is commented because HostServices is not working on Gluon mobiles.
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
    }

    public static void openInternalBrowser(String url, String displayedRoute) {
        FXBackgroundNode.setBackgroundNode(internalBrowser);
        // Loading the url in the internal browser
        internalBrowser.getEngine().load(url);
        // Going to the route to display that browser
        if (!REGISTERED_ROUTES.contains(displayedRoute)) {
            REGISTERED_ROUTES.add(displayedRoute);
            // Registering the website route in the ui router
            uiRouter.route(displayedRoute, () -> new ViewActivityBase<ViewActivityContextFinal>() {
                @Override
                public Node buildUi() {
                    return null; // => ModalityFrontOfficeMainFrameActivity is displaying the background node in this case
                }

                @Override
                public void onResume() {
                    FXCollapseMenu.setCollapseMenu(true);
                    super.onResume();
                }
            }, ViewActivityContext::create);
        }
        uiRouter.getHistory().push(displayedRoute);
    }

    public static void chooseHowToOpenWebsite(String url) {
        Hyperlink insideAppLink = GeneralUtility.createHyperlink(BrowserI18nKeys.openInsideApp, Color.WHITE, 21);
        Hyperlink outsideAppLink = GeneralUtility.createHyperlink(BrowserI18nKeys.openOutsideApp, Color.WHITE, 21);
        Hyperlink copyLink = GeneralUtility.createHyperlink(BrowserI18nKeys.copyLink, Color.WHITE, 21);
        VBox vBox = new VBox(30, insideAppLink, outsideAppLink, copyLink);
        vBox.setBorder(Border.stroke(Color.WHITE));
        vBox.setBackground(Background.fill(Brand.getBrandMainBackgroundColor()));
        vBox.setAlignment(Pos.CENTER);
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(vBox, FXMainFrameDialogArea.getDialogArea());
        vBox.setPadding(new Insets(50));
        insideAppLink.setOnAction(e -> {
            dialogCallback.closeDialog();
            openInternalBrowser(url, "/webview");
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
