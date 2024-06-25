// File managed by WebFX (DO NOT EDIT MANUALLY)
package dev.webfx.kit.registry.javafxweb;

import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class WebToJavaCallbacks {

    public static void bindCallbackMethods(Object javaInstance) {
        JsPropertyMap<Object> pm = Js.asPropertyMap(javaInstance);
        if (javaInstance instanceof one.modality.event.frontoffice.activities.booking.map.DynamicMapView) {
            one.modality.event.frontoffice.activities.booking.map.DynamicMapView castedInstance = (one.modality.event.frontoffice.activities.booking.map.DynamicMapView) javaInstance;
            pm.set("onGoogleMapLoaded", (JsVoidFn0Arg) castedInstance::onGoogleMapLoaded);
            pm.set("onMarkerClicked", (JsVoidFn1Arg<one.modality.event.frontoffice.activities.booking.map.MapMarker>) castedInstance::onMarkerClicked);
        } else if (javaInstance instanceof dev.webfx.extras.webview.pane.WebViewPane) {
            dev.webfx.extras.webview.pane.WebViewPane castedInstance = (dev.webfx.extras.webview.pane.WebViewPane) javaInstance;
            pm.set("consoleLog", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleLog);
            pm.set("consoleWarn", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleWarn);
            pm.set("consoleError", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleError);
        } else if (javaInstance instanceof one.modality.ecommerce.payment.client.WebPaymentForm) {
            one.modality.ecommerce.payment.client.WebPaymentForm castedInstance = (one.modality.ecommerce.payment.client.WebPaymentForm) javaInstance;
            pm.set("onInitSuccess", (JsVoidFn0Arg) castedInstance::onInitSuccess);
            pm.set("onInitFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onInitFailure);
            pm.set("onGatewayRecoveredFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayRecoveredFailure);
            pm.set("onGatewayFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayFailure);
            pm.set("onModalityFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onModalityFailure);
            pm.set("onFinalStatus", (JsVoidFn1Arg<java.lang.String>) castedInstance::onFinalStatus);
        }
    }


    @JsFunction
    public interface JsVoidFn0Arg {
        void apply();
    }

    @JsFunction
    public interface JsVoidFn1Arg<T1> {
        void apply(T1 arg1);
    }

}