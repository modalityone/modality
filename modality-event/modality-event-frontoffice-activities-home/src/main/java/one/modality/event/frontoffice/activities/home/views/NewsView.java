package one.modality.event.frontoffice.activities.home.views;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.fx.FXApp;
import one.modality.base.frontoffice.fx.FXHome;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.News;
import one.modality.event.frontoffice.operations.routes.home.RouteToHomeNewsArticleRequest;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class NewsView {
    News news;

    public NewsView(News news) {
        this.news = news;
    }

    public Node getView(VBox page, OperationActionFactoryMixin activityOperation, ViewDomainActivityBase activityBase) {
        Text t = TextUtility.getMainText(news.getTitle().toUpperCase(), StyleUtility.MAIN_BLUE);
        String date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(news.getDate());

        t.setOnMouseClicked(c -> {
            FXHome.article.set(news);
            activityOperation.executeOperation(new RouteToHomeNewsArticleRequest(activityBase.getHistory()));
        });

        Text c = TextUtility.getMainText(news.getExcerpt(), StyleUtility.VICTOR_BATTLE_BLACK);

        ImageView imgV = new ImageView();

        if (news.getImageUrl() != null)
            imgV.setImage(new Image(news.getImageUrl(), true));

        imgV.setPreserveRatio(true);
        FXProperties.runNowAndOnPropertiesChange(() -> imgV.setFitWidth(100* FXApp.fontRatio.get()), FXApp.fontRatio);

        VBox vList = GeneralUtility.createVList(5, 0,
                t,
                TextUtility.getSubText(date),
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
