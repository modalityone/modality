package one.modality.crm.backoffice2018.activities.letter;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.base.backoffice2018.controls.multilangeditor.MultiLanguageEditor;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.kit.util.properties.FXProperties;

/**
 * @author Bruno Salmon
 */
final class LetterActivity extends ViewDomainActivityBase implements ButtonFactoryMixin {

    private final Property<Object> routeLetterIdProperty = new SimpleObjectProperty<>();

    @Override
    public void onStart() {
        super.onStart();
        FXProperties.runOnPropertiesChange(() -> {
            if (isActive())
                onLetterChanged();
        }, routeLetterIdProperty, activeProperty());
    }

    @Override
    protected void updateModelFromContextParameters() {
        routeLetterIdProperty.setValue(getParameter("letterId"));
    }

    private MultiLanguageEditor multiLanguageEditor;
    @Override
    public Node buildUi() {
        multiLanguageEditor = new MultiLanguageEditor(this, routeLetterIdProperty::getValue, getDataSourceModel(), lang -> lang, lang -> "subject_" + lang, "Letter");
        return multiLanguageEditor.getUiNode();
    }

    private void onLetterChanged() {
        if (multiLanguageEditor != null)
            multiLanguageEditor.onEntityChanged();
    }
}
