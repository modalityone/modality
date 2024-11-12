package one.modality.crm.activities.magiclink;

import dev.webfx.extras.panes.GoldenRatioPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.text.Text;

final class MagicLinkActivity extends ViewDomainActivityBase {

    private final StringProperty tokenProperty = new SimpleStringProperty();

    @Override
    protected void updateModelFromContextParameters() {
        tokenProperty.set(getParameter(MagicLinkRouting.PATH_TOKEN_PARAMETER_NAME));
    }

    @Override
    public Node buildUi() { // Reminder: called only once (rebuild = bad UX) => UI is reacting to parameter changes

        // *************************************************************************************************************
        // ********************************* Building the static part of the UI ****************************************
        // *************************************************************************************************************

        Text text = new Text();
        GoldenRatioPane container = new GoldenRatioPane(text);
        container.setMaxWidth(Double.MAX_VALUE); // so it fills the whole width of the main frame VBox (with text centered)

        // *************************************************************************************************************
        // *********************************** Reacting to parameter changes *******************************************
        // *************************************************************************************************************

        FXProperties.runNowAndOnPropertyChange(token -> {
            text.setText("Magic link with token = " + token);
        }, tokenProperty);

        // *************************************************************************************************************
        // ************************************* Returning final container *********************************************
        // *************************************************************************************************************

        return container;
    }

}
