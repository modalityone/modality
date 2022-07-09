package org.modality_project.base.client.actions;

import org.modality_project.base.client.icons.ModalityIcons;
import dev.webfx.framework.client.ui.action.Action;
import dev.webfx.framework.client.ui.action.ActionBuilder;
import dev.webfx.framework.client.ui.action.ActionBuilderRegistry;
import dev.webfx.framework.client.ui.action.StandardActionKeys;

/**
 * @author Bruno Salmon
 */
public class ModalityActions {

    private final static Object VISIT_BOOK_ACTION_KEY = "Book>>";
    private final static Object VISIT_FEES_ACTION_KEY = "Fees>>";
    private final static Object VISIT_TERMS_AND_CONDITIONS_ACTION_KEY = "TermsAndConditions>>";
    private final static Object VISIT_PROGRAM_ACTION_KEY = "Program>>";
    private final static Object ADD_OPTION_ACTION_KEY = "AddOption";

    public static void registerActions() {
        registerAction(StandardActionKeys.ADD_ACTION_KEY, "Add", ModalityIcons.addIcon16JsonUrl);
        registerAction(StandardActionKeys.REMOVE_ACTION_KEY, "Remove", ModalityIcons.removeIcon16JsonUrl);
        registerAction(VISIT_BOOK_ACTION_KEY, VISIT_BOOK_ACTION_KEY, null);
        registerAction(VISIT_FEES_ACTION_KEY, VISIT_FEES_ACTION_KEY, ModalityIcons.priceTagMonoSvg16JsonUrl);
        registerAction(VISIT_TERMS_AND_CONDITIONS_ACTION_KEY, VISIT_TERMS_AND_CONDITIONS_ACTION_KEY, ModalityIcons.certificateMonoSvg16JsonUrl);
        registerAction(VISIT_PROGRAM_ACTION_KEY, VISIT_PROGRAM_ACTION_KEY, ModalityIcons.calendarMonoSvg16JsonUrl);
        registerAction(ADD_OPTION_ACTION_KEY, ADD_OPTION_ACTION_KEY, ModalityIcons.addIcon16JsonUrl);
    }

    private static void registerAction(Object key, Object i18nKey, String iconJsonUrl) {
        new ActionBuilder(key).setI18nKey(i18nKey).setGraphicUrlOrJson(iconJsonUrl).register();
    }

    public static Action newVisitTermsAndConditionsAction(Runnable handler) {
        return ActionBuilderRegistry.get().newAction(VISIT_TERMS_AND_CONDITIONS_ACTION_KEY, handler);
    }

    public static Action newVisitProgramAction(Runnable handler) {
        return ActionBuilderRegistry.get().newAction(VISIT_PROGRAM_ACTION_KEY, handler);
    }

    public static Action newVisitFeesAction(Runnable handler) {
        return ActionBuilderRegistry.get().newAction(VISIT_FEES_ACTION_KEY, handler);
    }

    public static Action newVisitBookAction(Runnable handler) {
        return ActionBuilderRegistry.get().newAction(VISIT_BOOK_ACTION_KEY, handler);
    }

    public static Action newAddOptionAction(Runnable handler) {
        return ActionBuilderRegistry.get().newAction(ADD_OPTION_ACTION_KEY, handler);
    }
}
