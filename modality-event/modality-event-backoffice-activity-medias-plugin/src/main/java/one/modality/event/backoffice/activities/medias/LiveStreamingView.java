package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


public class LiveStreamingView {
    private final MediasActivity activity;

    public LiveStreamingView(MediasActivity activity) {
        this.activity = activity;
    }


    public void startLogic() {

    }

    public Node buildContainer() {
        BorderPane mainFrame = new BorderPane();

        mainFrame.setPadding(new Insets(0,0,30,0));

        Label title = I18nControls.bindI18nProperties(new Label(), "LiveStreamingTitle");
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);

        return ControlUtil.createVerticalScrollPaneWithPadding(10, mainFrame);
    }

    public void setActive(boolean b) {
    }
}





