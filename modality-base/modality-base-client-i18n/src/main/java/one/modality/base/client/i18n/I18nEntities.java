package one.modality.base.client.i18n;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.lciimpl.EntityDomainReader;
import one.modality.base.client.entities.functions.I18nFunction;

/**
 * @author Bruno Salmon
 */
public final class I18nEntities {

    public static String translateEntity(Entity entity) {
        return translateEntity(entity, null);
    }

    public static String translateEntity(Entity entity, Object language) {
        if (entity == null)
            return null;
        return (String) I18nFunction.evaluate(entity, language, new EntityDomainReader<>(entity.getStore()));
    }
}
