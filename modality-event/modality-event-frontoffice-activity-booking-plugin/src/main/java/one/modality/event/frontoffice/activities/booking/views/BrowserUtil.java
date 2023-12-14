package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.routing.uirouter.UiRouter;
import dev.webfx.stack.routing.uirouter.activity.view.ViewActivityContext;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityBase;
import dev.webfx.stack.routing.uirouter.activity.view.impl.ViewActivityContextFinal;
import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * @author Bruno Salmon
 */
final class BrowserUtil {

    private static UiRouter uiRouter;
    private static final WebView internalBrowser = new WebView();

    static void openExternalBrowser(String url) {
        // Following code is commented because HostServices is not working on Gluon mobiles.
        //WebFxKitLauncher.getApplication().getHostServices().showDocument(url);
        // So we use the Browser service API instead (which as a Gluon implementation)
        try {
            Browser.launchExternalBrowser(url);
        } catch (Exception e) {
            Console.log(e);
        }
    }

    static void openInternalBrowser(String url, UiRouter uiRouter) {
        if (BrowserUtil.uiRouter == null) {
            BrowserUtil.uiRouter = uiRouter;
            uiRouter.route("/browser", () -> new ViewActivityBase<ViewActivityContextFinal>() {
                @Override
                public Node buildUi() {
                    return internalBrowser;
                }
            }, ViewActivityContext::create);
        }
        internalBrowser.getEngine().load(url);
        uiRouter.getHistory().push("/browser");
    }

}
