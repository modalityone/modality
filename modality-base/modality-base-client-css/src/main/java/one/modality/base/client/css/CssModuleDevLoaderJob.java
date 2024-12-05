package one.modality.base.client.css;

import dev.webfx.platform.boot.spi.ApplicationJob;

/**
 * The purpose of this class is just to enforce the load of the CSS module at the application start when running it with
 * OpenJFX in the IDE. Otherwise, resources such as fonts loaded via CSS (via webfx-css protocol) may not be found.
 *
 * @author Bruno Salmon
 */
public final class CssModuleDevLoaderJob implements ApplicationJob {
}
