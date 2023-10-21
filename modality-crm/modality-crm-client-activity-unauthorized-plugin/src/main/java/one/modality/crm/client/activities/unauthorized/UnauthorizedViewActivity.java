package one.modality.crm.client.activities.unauthorized;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.session.state.client.fx.FXAuthorizationsWaiting;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

/**
 * @author Bruno Salmon
 */
final class UnauthorizedViewActivity extends ViewDomainActivityBase {

    private static final String securityPath =
            // circle
            //"M 63.9999 5.72718 C 71.8769 5.72718 79.5076 7.26974 86.6789 10.3056 C 93.6205 13.2431 99.8563 17.4441 105.206 22.7938 C 110.556 28.1436 114.756 34.3794 117.694 41.3209 C 120.73 48.5087 122.273 56.1394 122.273 63.9999 C 122.273 71.8604 120.73 79.5076 117.694 86.6789 C 114.756 93.6205 110.556 99.8563 105.206 105.206 C 99.8563 110.556 93.6205 114.756 86.6789 117.694 C 79.4912 120.73 71.8604 122.273 63.9999 122.273 C 56.1394 122.273 48.4923 120.73 41.3209 117.694 C 34.3794 114.756 28.1436 110.556 22.7938 105.206 C 17.4441 99.8563 13.2431 93.6205 10.3056 86.6789 C 7.26974 79.4912 5.72718 71.8604 5.72718 63.9999 C 5.72718 56.1394 7.26974 48.4923 10.3056 41.3209 C 13.2431 34.3794 17.4441 28.1436 22.7938 22.7938 C 28.1436 17.4441 34.3794 13.2431 41.3209 10.3056 C 48.5087 7.26974 56.1394 5.72718 63.9999 5.72718 M 63.9999 0 C 28.6523 0 0 28.6523 0 63.9999 C 0 99.3476 28.6523 128 63.9999 128 C 99.3476 128 128 99.3476 128 63.9999 C 128 28.6523 99.3312 0 63.9999 0 Z " +
            "M0,64a64,64 0 1,0 128,0a64,64 0 1,0 -128,0 " +
                    // padlock bottom
            "M 49.7231 56.7138 C 48.2625 56.7138 47.0645 57.9117 47.0645 59.3722 L 47.0645 80.6564 C 47.0645 82.1168 48.2625 83.3148 49.7231 83.3148 L 78.0964 83.3148 C 79.5568 83.3148 80.7548 82.1168 80.7548 80.6564 L 80.7548 59.3722 C 80.7548 57.9117 79.5568 56.7138 78.0964 56.7138 Z M 49.7231 58.4861 L 78.0964 58.4861 C 78.5886 58.4861 78.9825 58.88 78.9825 59.3722 L 78.9825 80.6564 C 78.9825 81.1486 78.5886 81.5425 78.0964 81.5425 L 49.7231 81.5425 C 49.2307 81.5425 48.8369 81.1486 48.8369 80.6564 L 48.8369 59.3722 C 48.8369 58.88 49.2307 58.4861 49.7231 58.4861 Z M 63.9178 65.5753 C 62.4082 65.5753 61.2594 66.724 61.2594 68.2338 C 61.2594 69.0379 61.6205 69.7271 62.1456 70.1702 L 62.1456 72.6646 C 62.1456 73.6492 62.9497 74.4369 63.9178 74.4369 C 64.8861 74.4369 65.6902 73.6327 65.6902 72.6646 L 65.6902 70.1702 C 66.2318 69.7271 66.5764 69.0215 66.5764 68.2338 C 66.5764 66.724 65.4276 65.5753 63.9178 65.5753 Z " +
                    // padlock top
            "M 63.9999 44.8656 C 68.0369 44.8656 71.3189 48.1477 71.3189 52.1846 L 71.3189 56.1886 L 56.681 56.1886 L 56.681 52.1846 C 56.681 48.1477 59.963 44.8656 63.9999 44.8656 M 63.9999 42.2399 L 63.9999 42.2399 C 58.5025 42.2399 54.0553 46.6871 54.0553 52.1846 L 54.0553 58.8143 L 73.9445 58.8143 L 73.9445 52.1846 C 73.9445 46.6871 69.4973 42.2399 63.9999 42.2399 Z " +
                    // shield
            "M 63.2451 107.553 C63.2451,107.553 98.3811,90.5552 98.3811,40.4459L98.3811,39.1251 C 86.2194 37.5138 79.1466 33.8051 74.0102 31.0974 C 70.006 28.9969 67.1179 27.4707 63.9178 27.4707 C 60.7179 27.4707 57.8296 28.9969 53.8256 31.0974 C 48.6727 33.8051 41.6164 37.5138 29.6697 40.1395 L 28.4061 40.4184 L 28.4061 41.7148 C 28.4061 90.8963 62.9004 107.389 63.2451 107.553";

    @Override
    public Node buildUi() {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(securityPath);
        svgPath.setStroke(Color.rgb(0, 150, 214));
        svgPath.setStrokeWidth(2);
        svgPath.setFill(null);
        Text text = new Text();
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean waiting = FXAuthorizationsWaiting.isAuthorizationsWaiting();
            text.setText(waiting ? "Authorization check..." : "Sorry, you are not authorized to access this page");
        }, FXAuthorizationsWaiting.authorizationsWaitingProperty());
        ScalePane scalePane = new ScalePane(svgPath);
        scalePane.setFixedSize(256, 256);
        VBox vBox = new VBox(10, scalePane, text);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }
}
