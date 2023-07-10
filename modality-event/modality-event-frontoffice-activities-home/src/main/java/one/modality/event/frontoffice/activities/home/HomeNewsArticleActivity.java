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
        wv = new WebView();

        Button button = GeneralUtility.createButton(Color.web(StyleUtility.ELEMENT_GRAY), 4, "<--", 10);

        button.setOnAction(c -> {
            if (wv.getEngine().getHistory().getCurrentIndex() == 0) getHistory().goBack();
            else wv.getEngine().getHistory().go(-1);
        });

        wv.getEngine().load(FXHome.article.get().url);

        StackPane.setAlignment(button, Pos.TOP_LEFT);
        StackPane.setMargin(button, new Insets(10));
        layers.getChildren().addAll(wv, button);
    }

    @Override
    public Node buildUi() {
        rebuild();

        FXHome.article.addListener(c -> {
            rebuild();
        });

        return layers;
    }
}
