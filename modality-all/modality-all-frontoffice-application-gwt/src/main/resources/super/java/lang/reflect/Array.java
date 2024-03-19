// File managed by WebFX (DO NOT EDIT MANUALLY)
package java.lang.reflect;

import dev.webfx.platform.console.Console;
import jsinterop.base.Js;

public final class Array {

    public static Object newInstance(Class<?> componentType, int length) throws NegativeArraySizeException {
        switch (componentType.getName()) {
            case "dev.webfx.stack.db.query.QueryResult": return new dev.webfx.stack.db.query.QueryResult[length];
            case "dev.webfx.stack.db.submit.SubmitResult": return new dev.webfx.stack.db.submit.SubmitResult[length];

            // TYPE NOT FOUND
            default:
               Console.log("GWT super source Array.newInstance() has no case for type " + componentType + ", so new Object[] is returned but this may cause a ClassCastException.");
               return new Object[length];
        }
    }

    public static int getLength(Object array) {
        return Js.asArray(array).length;
    }

}