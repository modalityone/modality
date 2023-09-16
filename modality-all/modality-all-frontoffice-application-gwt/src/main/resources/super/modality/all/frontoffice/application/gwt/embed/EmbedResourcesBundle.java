// File managed by WebFX (DO NOT EDIT MANUALLY)
package modality.all.frontoffice.application.gwt.embed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface EmbedResourcesBundle extends ClientBundle {

    EmbedResourcesBundle R = GWT.create(EmbedResourcesBundle.class);
    @Source("dev/webfx/platform/conf/src-root.properties")
    TextResource r1();

    @Source("dev/webfx/platform/meta/exe/exe.properties")
    TextResource r2();

    @Source("one/modality/base/client/services/i18n/dictionaries/en.json")
    TextResource r3();

    @Source("one/modality/base/client/services/i18n/dictionaries/fr.json")
    TextResource r4();

    @Source("one/modality/base/shared/domainmodel/DomainModelSnapshot.json")
    TextResource r5();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("dev/webfx/platform/conf/src-root.properties", R.r1());
            registerResource("dev/webfx/platform/meta/exe/exe.properties", R.r2());
            registerResource("one/modality/base/client/services/i18n/dictionaries/en.json", R.r3());
            registerResource("one/modality/base/client/services/i18n/dictionaries/fr.json", R.r4());
            registerResource("one/modality/base/shared/domainmodel/DomainModelSnapshot.json", R.r5());

        }
    }
}
