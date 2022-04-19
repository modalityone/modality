package mongoose.ecommerce.backoffice.operations.entities.document.cart;

import javafx.scene.layout.Pane;
import mongoose.base.shared.entities.Document;
import dev.webfx.framework.client.ui.controls.alert.AlertUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.platform.shared.util.async.Future;

final class OpenBookingCartExecutor {

    static Future<Void> executeRequest(OpenBookingCartRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        document.evaluateOnceLoaded("cartUrl")
                .onFailure(cause -> AlertUtil.showExceptionAlert(cause, parentContainer.getScene().getWindow()))
                .onSuccess(cartUrl -> WebFxKitLauncher.getApplication().getHostServices().showDocument(cartUrl.toString()));
        return Future.succeededFuture();
    }
}
