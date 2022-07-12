package org.modality_project.base.client.services.i18n;


import javafx.scene.Node;
import dev.webfx.stack.i18n.I18nPart;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.i18n.spi.impl.json.JsonI18nProvider;
import dev.webfx.stack.ui.util.image.JsonImageViews;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;

/**
 * @author Bruno Salmon
 */
public final class ModalityI18nProvider extends JsonI18nProvider {

    public ModalityI18nProvider() {
        super("org/modality_project/base/client/services/i18n/dictionaries/{lang}.json");
    }

    @Override
    public Node createI18nGraphic(String graphicUrl) {
        return JsonImageViews.createImageView(graphicUrl);
    }

    @Override
    public String interpretToken(Object i18nKey, I18nPart part, String token) {
        if (token.startsWith("expression:")) {
            Entity entity = findEntity(i18nKey);
            if (entity != null)
                return (String) entity.evaluate(token.substring(11));
        }
        return super.interpretToken(i18nKey, part, token);
    }

    private Entity findEntity(Object i18nKey) {
        if (i18nKey instanceof HasEntity)
            return ((HasEntity) i18nKey).getEntity();
        if (i18nKey instanceof I18nSubKey)
            return findEntity(((I18nSubKey) i18nKey).getParentI18nKey());
        return null;
    }
}
