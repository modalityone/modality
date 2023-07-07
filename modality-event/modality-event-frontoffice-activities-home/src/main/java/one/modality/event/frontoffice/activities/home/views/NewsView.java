package one.modality.event.frontoffice.activities.home.views;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.entities.News;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.event.frontoffice.operations.routes.home.RouteToHomeNewsArticleRequest;

public class NewsView {
    News news;

    public NewsView(News news) {
        this.news = news;
    }

    public Node getView(VBox page, OperationActionFactoryMixin activityOperation, ViewDomainActivityBase activityBase) {
        Text t = TextUtility.getMainText(news.title.toUpperCase(), StyleUtility.MAIN_BLUE);

        t.setOnMouseClicked(c -> {
            FXHome.article.set(news);
            activityOperation.executeOperation(new RouteToHomeNewsArticleRequest(activityBase.getHistory()));
        });

        Text c = TextUtility.getMainText(news.excerpt, StyleUtility.VICTOR_BATTLE_BLACK);

        ImageView imgV = new ImageView();

        Fetch.fetch("https://kadampa.org/wp-json/wp/v2/media/" + news.mediaId) // Expecting a JSON object only
                .onFailure(error -> Console.log("Fetch failure: " + error))
                .onSuccess(response -> {
                    Console.log("Fetch success: ok = " + response.ok());
                    response.jsonObject()
                            .onFailure(error -> Console.log("JsonObject failure: " + error))
                            .onSuccess(jsonObject -> {
                                imgV.setImage(new Image(jsonObject.getObject("guid").getString("rendered").replace("\\", ""), true));
                            });
                });

        imgV.setPreserveRatio(true);
        FXProperties.runNowAndOnPropertiesChange(() -> imgV.setFitWidth(100* FXApp.fontRatio.get()), FXApp.fontRatio);

        VBox vList = GeneralUtility.createVList(5, 0,
                t,
                TextUtility.getSubText(news.date),
                c, GeneralUtility.createSpace(20));
        vList.setMinWidth(0);
        HBox.setHgrow(vList, Priority.ALWAYS);
        vList.widthProperty().addListener((observableValue, number, width) -> {
            double wrappingWidth = width.doubleValue() - 10;
            t.setWrappingWidth(wrappingWidth);
            c.setWrappingWidth(wrappingWidth);
        });

        HBox newsBanner = GeneralUtility.createHList(10, 0, imgV, vList);
        newsBanner.maxWidthProperty().bind(page.widthProperty());

        return newsBanner;
    }
}
