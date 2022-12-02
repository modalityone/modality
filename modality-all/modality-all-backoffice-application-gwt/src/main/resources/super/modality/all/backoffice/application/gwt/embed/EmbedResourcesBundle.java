// File managed by WebFX (DO NOT EDIT MANUALLY)
package modality.all.backoffice.application.gwt.embed;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface EmbedResourcesBundle extends ClientBundle {

    EmbedResourcesBundle R = GWT.create(EmbedResourcesBundle.class);
    @Source("images/svg/color/price-tag.svg")
    TextResource r1();

    @Source("images/svg/mono/calendar.svg")
    TextResource r2();

    @Source("images/svg/mono/certificate.svg")
    TextResource r3();

    @Source("images/svg/mono/price-tag.svg")
    TextResource r4();

    @Source("one/modality/base/client/busconfig/ClientBusOptions.default.json")
    TextResource r5();

    @Source("one/modality/base/client/services/i18n/dictionaries/en.json")
    TextResource r6();

    @Source("one/modality/base/client/services/i18n/dictionaries/fr.json")
    TextResource r7();

    @Source("one/modality/base/shared/domainmodel/DomainModelSnapshot.json")
    TextResource r8();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("images/svg/color/price-tag.svg", R.r1());
            registerResource("images/svg/mono/calendar.svg", R.r2());
            registerResource("images/svg/mono/certificate.svg", R.r3());
            registerResource("images/svg/mono/price-tag.svg", R.r4());
            registerResource("one/modality/base/client/busconfig/ClientBusOptions.default.json", R.r5());
            registerResource("one/modality/base/client/services/i18n/dictionaries/en.json", R.r6());
            registerResource("one/modality/base/client/services/i18n/dictionaries/fr.json", R.r7());
            registerResource("one/modality/base/shared/domainmodel/DomainModelSnapshot.json", R.r8());

        }
    }
}
