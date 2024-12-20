package one.modality.base.client.brand;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import dev.webfx.platform.resource.Resource;

/**
 * @author Bruno Salmon
 */
public class Brand {

    //TODO: update application code to stop using these methods from Java and rely only on CSS and i18n

    public static final Color BRAND_MAIN_COLOR = Color.web("#0096D6"); // Orange

    public static final Color BRAND_MAIN_BACKGROUND_COLOR = Color.web("#0096D6"); // Blue

    public static Color getBrandMainColor() {
        return BRAND_MAIN_COLOR; // Orange
    }

    public static Color getBrandMainBackgroundColor() {
        return BRAND_MAIN_BACKGROUND_COLOR; // Blue
    }

    public static Node createModalityBackOfficeBrandNode() {
        ImageView logo = new ImageView(Resource.toUrl("modality-logo.png", Brand.class));
        Text modality = new Text("modality");
        Text one = new Text("one");
        modality.setFont(Font.font("Montserrat", FontWeight.NORMAL, 18));
        one.setFont(Font.font("Montserrat", FontWeight.BOLD, 18));
        modality.setFill(Color.web("4D4D4D"));
        one.setFill(Color.web("1589BF"));
        HBox brand = new HBox(logo, modality, one);
        HBox.setMargin(logo, new Insets(0, 3, 0, 0)); // 3px gap between logo and text
        brand.setAlignment(Pos.CENTER);
        return brand;
    }


}
