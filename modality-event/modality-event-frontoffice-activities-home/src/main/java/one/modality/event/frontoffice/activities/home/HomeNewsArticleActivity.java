package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;

public class HomeNewsArticleActivity extends ViewDomainActivityBase implements OperationActionFactoryMixin {
    private WebView wv = new WebView();
    private StackPane layers = new StackPane();

    private void rebuild() {
        System.out.println(wv.getEngine().getLocation());
        layers.getChildren().clear();

        Button button = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "<--", 10);

        button.setOnAction(c -> {
            int i = wv.getEngine().getHistory().getCurrentIndex();

            if (wv.getEngine().getLocation().isEmpty()) getHistory().goBack();
            else if (i == 0) rebuild();
            else wv.getEngine().getHistory().go(-1);
        });

        String html = "<div style='text-align:center'><div style='display:inline-block'><div style='max-width:500px; text-align:left'><div style='max-width: 90%'>" +
                FXHome.article.get().content.replace("\\", "").replaceAll(
                        "width=\".*\"", "width=\"90%\""
                ).replaceAll(
                        "height=\".*\"", ""
                ) +
                "</div></div></div></div>";

        wv.getEngine().loadContent(html, "text/html");

        StackPane.setAlignment(button, Pos.TOP_LEFT);
        StackPane.setMargin(button, new Insets(10));
        layers.getChildren().addAll(wv, button);
    }

    @Override
    public Node buildUi() {
        wv.getEngine().getHistory().setMaxSize(20);

        rebuild();

        FXHome.article.addListener(c -> {
            rebuild();
        });

        return layers;
    }
}
