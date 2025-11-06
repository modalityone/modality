package one.modality.event.backoffice.activities.program;


import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.shared.knownitems.KnownItemFamily;

/**
 * @author Bruno Salmon
 */
final class ProgramActivity extends ViewDomainActivityBase {

    private ProgramView programView;

    @Override
    public void onResume() {
        super.onResume();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    @Override
    protected void startLogic() {
        programView = new ProgramView(new ProgramModel(KnownItemFamily.TEACHING, getDataSourceModel()));
        programView.startLogic();
    }

    @Override
    public Node buildUi() {
        Label title = I18nControls.newLabel(ProgramI18nKeys.ProgramTitle);
        title.setContentDisplay(ContentDisplay.TOP);
        title.setPadding(new Insets(30));
        title.setGraphicTextGap(30);
        title.getStyleClass().add(Bootstrap.H2);
        TextTheme.createPrimaryTextFacet(title).style();

        BorderPane mainFrame = new BorderPane(programView.getView());

        BorderPane.setAlignment(title, Pos.CENTER);
        mainFrame.setTop(title);
        mainFrame.setPadding(new Insets(0, 20, 30, 20));

        return Controls.createVerticalScrollPane(mainFrame);
    }
}

