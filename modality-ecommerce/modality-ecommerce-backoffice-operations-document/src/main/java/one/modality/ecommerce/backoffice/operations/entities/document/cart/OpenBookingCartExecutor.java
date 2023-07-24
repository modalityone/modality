package one.modality.ecommerce.backoffice.operations.entities.document.cart;

import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.ui.controls.alert.AlertUtil;

import javafx.scene.layout.Pane;

import one.modality.base.shared.entities.Document;

final class OpenBookingCartExecutor {

    static Future<Void> executeRequest(OpenBookingCartRequest rq) {
        return execute(rq.getDocument(), rq.getParentContainer());
    }

    private static Future<Void> execute(Document document, Pane parentContainer) {
        document.evaluateOnceLoaded("cartUrl")
                .onFailure(
                        cause ->
                                AlertUtil.showExceptionAlert(
                                        cause, parentContainer.getScene().getWindow()))
                .onSuccess(
                        cartUrl ->
                                WebFxKitLauncher.getApplication()
                                        .getHostServices()
                                        .showDocument(cartUrl.toString()));
        return Future.succeededFuture();
    }
}
