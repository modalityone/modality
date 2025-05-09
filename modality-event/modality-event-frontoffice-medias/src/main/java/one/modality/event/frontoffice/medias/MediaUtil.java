package one.modality.event.frontoffice.medias;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.lciimpl.EntityDomainReader;
import one.modality.base.client.entities.functions.I18nFunction;

public class MediaUtil {

    public static String translate(Entity entity) {
        return translate(entity, null);
    }

    public static String translate(Entity entity, Object language) {
        if (entity == null)
            return null;
        return (String) I18nFunction.evaluate(entity, language, new EntityDomainReader<>(entity.getStore()));
    }
}
