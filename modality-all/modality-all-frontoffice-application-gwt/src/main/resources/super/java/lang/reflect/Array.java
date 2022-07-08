// File managed by WebFX (DO NOT EDIT MANUALLY)
package java.lang.reflect;

import dev.webfx.platform.shared.services.log.Logger;

public final class Array {

    public static Object newInstance(Class<?> componentType, int length) throws NegativeArraySizeException {
        switch (componentType.getName()) {
            case "dev.webfx.platform.shared.services.query.QueryResult": return new dev.webfx.platform.shared.services.query.QueryResult[length];
            case "dev.webfx.platform.shared.services.submit.SubmitResult": return new dev.webfx.platform.shared.services.submit.SubmitResult[length];

            // TYPE NOT FOUND
            default:
               Logger.log("GWT super source Array.newInstance() has no case for type " + componentType + ", so new Object[] is returned but this may cause a ClassCastException.");
               return new Object[length];
        }
    }

    public static int getLength(Object array) {
        return asArray(array).length;
    }

    // From com.google.gwt.lang.Array (gwt-dev:2.9.0)
    /**
     * Use JSNI to effect a castless type change.
     */
    private static native <T> T[] asArray(Object array) /*-{
        return array;
    }-*/;

}