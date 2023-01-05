// File managed by WebFX (DO NOT EDIT MANUALLY)
package modality.all.frontoffice.application.gwt.embed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface EmbedResourcesBundle extends ClientBundle {

    EmbedResourcesBundle R = GWT.create(EmbedResourcesBundle.class);
    @Source("dev/webfx/platform/meta/exe/exe.properties")
    TextResource r1();

    @Source("images/svg/color/price-tag.svg")
    TextResource r2();

    @Source("images/svg/mono/calendar.svg")
    TextResource r3();

    @Source("images/svg/mono/certificate.svg")
    TextResource r4();

    @Source("images/svg/mono/price-tag.svg")
    TextResource r5();

    @Source("one/modality/base/client/busconfig/ClientBusOptions.default.json")
    TextResource r6();

    @Source("one/modality/base/client/services/i18n/dictionaries/en.json")
    TextResource r7();

    @Source("one/modality/base/client/services/i18n/dictionaries/fr.json")
    TextResource r8();

    @Source("one/modality/base/shared/domainmodel/DomainModelSnapshot.json")
    TextResource r9();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("dev/webfx/platform/meta/exe/exe.properties", R.r1());
            registerResource("images/svg/color/price-tag.svg", R.r2());
            registerResource("images/svg/mono/calendar.svg", R.r3());
            registerResource("images/svg/mono/certificate.svg", R.r4());
            registerResource("images/svg/mono/price-tag.svg", R.r5());
            registerResource("one/modality/base/client/busconfig/ClientBusOptions.default.json", R.r6());
            registerResource("one/modality/base/client/services/i18n/dictionaries/en.json", R.r7());
            registerResource("one/modality/base/client/services/i18n/dictionaries/fr.json", R.r8());
            registerResource("one/modality/base/shared/domainmodel/DomainModelSnapshot.json", R.r9());

        }
    }
}
