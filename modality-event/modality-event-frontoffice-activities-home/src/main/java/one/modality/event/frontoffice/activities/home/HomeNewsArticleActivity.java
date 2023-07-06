package one.modality.event.frontoffice.activities.home;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.scene.Node;
import javafx.scene.web.WebView;
import one.modality.base.frontoffice.fx.FXHome;

public class HomeNewsArticleActivity extends ViewDomainActivityBase {
    @Override
    public Node buildUi() {
        WebView wv = new WebView();

        String html = "<div style='text-align:center'><div style='display:inline-block'><div style='max-width:500px; text-align:left'><div style='max-width: 90%'>" +
                FXHome.article.content.replace("\\", "").replaceAll(
                        "width=\".*\"", "width=\"90%\""
                ).replaceAll(
                        "height=\".*\"", ""
                ) +
                "</div></div></div></div>";

        System.out.println(html);

        wv.getEngine().loadContent(html, "text/html");
//        wv.getEngine().setUserStyleSheetLocation(Objects.requireNonNull(getClass().getResource()).toString());

        return wv;
    }
}
