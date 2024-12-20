// File managed by WebFX (DO NOT EDIT MANUALLY)
package dev.webfx.kit.registry.javafxweb;

import jsinterop.annotations.JsFunction;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

public class WebToJavaCallbacks {

    public static void bindCallbackMethods(Object javaInstance) {
        JsPropertyMap<Object> pm = Js.asPropertyMap(javaInstance);
        if (javaInstance instanceof dev.webfx.extras.player.video.web.SeamlessCapableWebVideoPlayer) {
            dev.webfx.extras.player.video.web.SeamlessCapableWebVideoPlayer castedInstance = (dev.webfx.extras.player.video.web.SeamlessCapableWebVideoPlayer) javaInstance;
            pm.set("onReady", (JsVoidFn0Arg) castedInstance::onReady);
            pm.set("onPlay", (JsVoidFn0Arg) castedInstance::onPlay);
            pm.set("onPause", (JsVoidFn0Arg) castedInstance::onPause);
            pm.set("onEnd", (JsVoidFn0Arg) castedInstance::onEnd);
        } else if (javaInstance instanceof dev.webfx.extras.webview.pane.WebViewPane) {
            dev.webfx.extras.webview.pane.WebViewPane castedInstance = (dev.webfx.extras.webview.pane.WebViewPane) javaInstance;
            pm.set("consoleLog", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleLog);
            pm.set("consoleWarn", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleWarn);
            pm.set("consoleError", (JsVoidFn1Arg<java.lang.String>) castedInstance::consoleError);
        } else if (javaInstance instanceof one.modality.event.frontoffice.activities.booking.map.DynamicMapView) {
            one.modality.event.frontoffice.activities.booking.map.DynamicMapView castedInstance = (one.modality.event.frontoffice.activities.booking.map.DynamicMapView) javaInstance;
            pm.set("onGoogleMapLoaded", (JsVoidFn0Arg) castedInstance::onGoogleMapLoaded);
            pm.set("onMarkerClicked", (JsVoidFn1Arg<one.modality.event.frontoffice.activities.booking.map.MapMarker>) castedInstance::onMarkerClicked);
        } else if (javaInstance instanceof one.modality.ecommerce.payment.client.WebPaymentForm) {
            one.modality.ecommerce.payment.client.WebPaymentForm castedInstance = (one.modality.ecommerce.payment.client.WebPaymentForm) javaInstance;
            pm.set("onGatewayInitSuccess", (JsVoidFn0Arg) castedInstance::onGatewayInitSuccess);
            pm.set("onGatewayInitFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayInitFailure);
            pm.set("onGatewayCardVerificationFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayCardVerificationFailure);
            pm.set("onGatewayBuyerVerificationFailure", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayBuyerVerificationFailure);
            pm.set("onGatewayPaymentVerificationSuccess", (JsVoidFn1Arg<java.lang.String>) castedInstance::onGatewayPaymentVerificationSuccess);
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