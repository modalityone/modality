package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.platform.browser.Browser;
import dev.webfx.platform.console.Console;

/**
 * @author Bruno Salmon
 */
final class BrowserUtil {

    static void openBrowser(String url) {
        // Following code is commented because HostServices is not working on Gluon mobiles.
        //WebFxKitLauncher.getApplication().getHostServices().showDocument(url);
        // So we use the Browser service API instead (which as a Gluon implementation)
        try {
            Browser.launchExternalBrowser(url);
        } catch (Exception e) {
            Console.log(e);
        }
    }
}
