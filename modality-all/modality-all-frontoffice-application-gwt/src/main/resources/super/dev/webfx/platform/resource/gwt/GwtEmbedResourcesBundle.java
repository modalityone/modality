// File managed by WebFX (DO NOT EDIT MANUALLY)
package dev.webfx.platform.resource.gwt;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundleBase;

public interface GwtEmbedResourcesBundle extends ClientBundle {

    GwtEmbedResourcesBundle R = GWT.create(GwtEmbedResourcesBundle.class);
    @Source("dev/webfx/extras/webview/pane/WebViewPane.js")
    TextResource r1();

    @Source("dev/webfx/platform/conf/src-root.properties")
    TextResource r2();

    @Source("dev/webfx/platform/meta/exe/exe.properties")
    TextResource r3();

    @Source("dev/webfx/stack/i18n/en.properties")
    TextResource r4();

    @Source("dev/webfx/stack/i18n/fr.properties")
    TextResource r5();

    @Source("one/modality/base/shared/domainmodel/DomainModelSnapshot.json")
    TextResource r6();

    @Source("one/modality/event/frontoffice/activities/booking/map/DynamicMapView.js")
    TextResource r7();



    final class ProvidedGwtResourceBundle extends GwtResourceBundleBase {
        public ProvidedGwtResourceBundle() {
            registerResource("dev/webfx/extras/webview/pane/WebViewPane.js", R.r1());
            registerResource("dev/webfx/platform/conf/src-root.properties", R.r2());
            registerResource("dev/webfx/platform/meta/exe/exe.properties", R.r3());
            registerResource("dev/webfx/stack/i18n/en.properties", R.r4());
            registerResource("dev/webfx/stack/i18n/fr.properties", R.r5());
            registerResource("one/modality/base/shared/domainmodel/DomainModelSnapshot.json", R.r6());
            registerResource("one/modality/event/frontoffice/activities/booking/map/DynamicMapView.js", R.r7());

        }
    }
}
