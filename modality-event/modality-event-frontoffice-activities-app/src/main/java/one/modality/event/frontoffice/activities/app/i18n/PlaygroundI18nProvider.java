package one.modality.event.frontoffice.activities.app.i18n;

import dev.webfx.stack.i18n.spi.impl.json.JsonI18nProvider;

import java.util.Collection;
import java.util.List;

public class PlaygroundI18nProvider extends JsonI18nProvider {

    public PlaygroundI18nProvider() {
        super("i18n/{lang}.json", "en");
    }

    @Override
    public Collection<Object> getSupportedLanguages() {
        return List.of("en", "fr");
    }
}
