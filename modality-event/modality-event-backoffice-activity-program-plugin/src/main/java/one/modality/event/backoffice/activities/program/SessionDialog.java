package one.modality.event.backoffice.activities.program;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.LabelTextField;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.client.util.dialog.ModalityDialog;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static one.modality.event.backoffice.activities.program.ProgramI18nKeys.AddSession;
import static one.modality.event.backoffice.activities.program.ProgramI18nKeys.SaveProgram;

/**
 * Dialog for creating and editing program sessions (ScheduledItem).
 * Allows editing session name, time range, and audio/video availability.
 *
 * @author Claude Code
 */
public class SessionDialog {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Shows the create/edit session dialog.
     *
     * @param scheduledItem Existing session to edit (null for new session)
     * @param date Date for the new session (used only when creating)
     * @param entityStore Entity store for creating entities
     * @param programModel Program model for accessing bookable scheduled items
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(ScheduledItem scheduledItem, LocalDate date, EntityStore entityStore, ProgramModel programModel, Runnable onSuccess) {
        boolean isEdit = scheduledItem != null;

        // Create a local UpdateStore for this dialog session.
        // This allows users to cancel the dialog without affecting other unsaved changes in ProgramModel.
        // When saved, changes are submitted immediately to the database.
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);

        // Get the entity to edit (create a temporary one for new sessions)
        ScheduledItem entityForDialog;
        if (isEdit) {
            entityForDialog = updateStore.updateEntity(scheduledItem);
        } else {
            // Create a temporary entity for the LabelTextField
            entityForDialog = updateStore.insertEntity(ScheduledItem.class);
        }

        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(600);
        dialogContent.setMaxWidth(700);

        // Header with title
        Label titleLabel = Bootstrap.h3(Bootstrap.strong(I18nControls.newLabel(isEdit ? ProgramI18nKeys.EditSession : AddSession)));

        // Form fields
        VBox formFields = new VBox(16);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Session name field using LabelTextField
        VBox nameFieldBox = new VBox(8);
        Label nameLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.SessionName));

        // For edit mode: if the scheduled item doesn't have a name but has a timeline with an item,
        // copy the timeline's item name to the scheduled item's name field
        if (isEdit && (entityForDialog.getName() == null || entityForDialog.getName().isEmpty())) {
            if (scheduledItem.getTimeline() != null && scheduledItem.getTimeline().getItem() != null) {
                entityForDialog.setName(scheduledItem.getTimeline().getItem().getName());
            }
        }

        LabelTextField nameField = new LabelTextField(entityForDialog, "name", "label", updateStore);
        nameFieldBox.getChildren().addAll(nameLabel, nameField.getView());

        // Time range fields
        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(12);
        timeGrid.setVgap(8);

        Label startTimeLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.StartTime));
        TextField startTimeInput = new TextField();
        startTimeInput.setPromptText("HH:mm");
        startTimeInput.setPrefWidth(100);

        Label endTimeLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.EndTime));
        TextField endTimeInput = new TextField();
        endTimeInput.setPromptText("HH:mm");
        endTimeInput.setPrefWidth(100);

        // Load existing times from ScheduledItem first, then Timeline as fallback
        if (isEdit) {
            LocalTime startTimeValue = scheduledItem.getStartTime();
            if (startTimeValue == null && scheduledItem.getTimeline() != null) {
                startTimeValue = scheduledItem.getTimeline().getStartTime();
            }
            if (startTimeValue != null) {
                startTimeInput.setText(startTimeValue.format(TIME_FORMATTER));
            }

            LocalTime endTimeValue = scheduledItem.getEndTime();
            if (endTimeValue == null && scheduledItem.getTimeline() != null) {
                endTimeValue = scheduledItem.getTimeline().getEndTime();
            }
            if (endTimeValue != null) {
                endTimeInput.setText(endTimeValue.format(TIME_FORMATTER));
            }
        }

        timeGrid.add(startTimeLabel, 0, 0);
        timeGrid.add(startTimeInput, 0, 1);
        timeGrid.add(endTimeLabel, 1, 0);
        timeGrid.add(endTimeInput, 1, 1);

        // Audio/Video checkboxes
        VBox availabilityBox = new VBox(12);
        Label availabilityLabel = Bootstrap.strong(I18nControls.newLabel(ProgramI18nKeys.Availability));

        CheckBox audioCheck = I18nControls.newCheckBox(ProgramI18nKeys.AudioOffered);
        CheckBox videoCheck = I18nControls.newCheckBox(ProgramI18nKeys.VideoOffered);

        // Flag to prevent listeners from triggering during initial setup
        final SimpleBooleanProperty initialSetupComplete = new SimpleBooleanProperty(false);

        // Track initial checkbox values (checkboxes don't map to entity fields - they control child scheduledItems)
        final SimpleBooleanProperty initialAudioOffered = new SimpleBooleanProperty(false);
        final SimpleBooleanProperty initialVideoOffered = new SimpleBooleanProperty(false);

        // Trigger property for binding re-evaluation when updateStore changes
        // (LabelTextField doesn't expose textProperty, so we poll the updateStore)
        final SimpleBooleanProperty storeHasChangesTrigger = new SimpleBooleanProperty(false);

        // Poll updateStore periodically to detect changes (for name field and other non-observable changes)
        // This ensures save button becomes enabled when user types in the name field
        Scheduled pollTask = Scheduler.schedulePeriodic(300, () -> {
            if (initialSetupComplete.get() && isEdit) {
                storeHasChangesTrigger.set(updateStore.hasChanges());
            }
        });

        // Add listeners to update entity dynamically (like LabelTextField does)
        // Start time listener - updates entity immediately
        startTimeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (initialSetupComplete.get()) {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    try {
                        LocalTime time = LocalTime.parse(newVal.trim(), TIME_FORMATTER);
                        entityForDialog.setStartTime(time);
                    } catch (DateTimeParseException e) {
                        // Invalid format - don't update entity (validation will show error)
                    }
                } else {
                    // Empty field - clear the value
                    entityForDialog.setStartTime(null);
                }
            }
        });

        // End time listener - updates entity immediately
        endTimeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (initialSetupComplete.get()) {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    try {
                        LocalTime time = LocalTime.parse(newVal.trim(), TIME_FORMATTER);
                        entityForDialog.setEndTime(time);
                    } catch (DateTimeParseException e) {
                        // Invalid format - don't update entity (validation will show error)
                    }
                } else {
                    // Empty field - clear the value
                    entityForDialog.setEndTime(null);
                }
            }
        });

        // Track initial state and check for existing child scheduledItems with media
        if (isEdit) {
            // Query for existing audio and video child scheduledItems to determine initial state
            entityStore.executeQuery(
                "select id from ScheduledItem where programScheduledItem=$1 and item.family.code=$2",
                scheduledItem, KnownItemFamily.AUDIO_RECORDING.getCode()
            ).onSuccess(audioScheduledItems -> {
                boolean audioOfferedValue = audioScheduledItems.size() > 0;
                audioCheck.setSelected(audioOfferedValue);
                initialAudioOffered.set(audioOfferedValue);

                entityStore.executeQuery(
                    "select id from ScheduledItem where programScheduledItem=$1 and item.family.code=$2",
                    scheduledItem, KnownItemFamily.VIDEO.getCode()
                ).onSuccess(videoScheduledItems -> {
                    boolean videoOfferedValue = videoScheduledItems.size() > 0;
                    videoCheck.setSelected(videoOfferedValue);
                    initialVideoOffered.set(videoOfferedValue);

                    // Query for existing child scheduledItems with media to warn user
                    checkAndWarnForMediaDeletion(scheduledItem, audioCheck, videoCheck, audioOfferedValue, videoOfferedValue, entityStore);

                    // Initial setup complete - listeners can now update entity
                    initialSetupComplete.set(true);
                });
            });
        } else {
            // For new sessions, default to both audio and video offered
            audioCheck.setSelected(true);
            videoCheck.setSelected(true);
            initialAudioOffered.set(true);
            initialVideoOffered.set(true);
            // Initial setup complete - listeners can now update entity
            initialSetupComplete.set(true);
        }

        availabilityBox.getChildren().addAll(availabilityLabel, audioCheck, videoCheck);

        // Add all fields to form
        formFields.getChildren().addAll(nameFieldBox, timeGrid, availabilityBox);

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();

        // Add required validation for start time
        validationSupport.addRequiredInput(startTimeInput, I18n.i18nTextProperty(ProgramI18nKeys.StartTimeRequired));

        // Add required validation for end time
        validationSupport.addRequiredInput(endTimeInput, I18n.i18nTextProperty(ProgramI18nKeys.EndTimeRequired));

        // Add time format validation for start time
        validationSupport.addValidationRule(
            Bindings.createBooleanBinding(() -> {
                String text = startTimeInput.getText().trim();
                if (text.isEmpty()) return true; // Let required validation handle empty
                try {
                    LocalTime.parse(text, TIME_FORMATTER);
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            }, startTimeInput.textProperty()),
            startTimeInput,
            I18n.i18nTextProperty(ProgramI18nKeys.InvalidTimeFormat)
        );

        // Add time format validation for end time
        validationSupport.addValidationRule(
            Bindings.createBooleanBinding(() -> {
                String text = endTimeInput.getText().trim();
                if (text.isEmpty()) return true; // Let required validation handle empty
                try {
                    LocalTime.parse(text, TIME_FORMATTER);
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            }, endTimeInput.textProperty()),
            endTimeInput,
            I18n.i18nTextProperty(ProgramI18nKeys.InvalidTimeFormat)
        );

        // Add validation that end time must be after start time
        validationSupport.addValidationRule(
            Bindings.createBooleanBinding(() -> {
                String startText = startTimeInput.getText().trim();
                String endText = endTimeInput.getText().trim();
                if (startText.isEmpty() || endText.isEmpty()) return true; // Let required validation handle empty
                try {
                    LocalTime startTime = LocalTime.parse(startText, TIME_FORMATTER);
                    LocalTime endTime = LocalTime.parse(endText, TIME_FORMATTER);
                    return startTime.isBefore(endTime);
                } catch (DateTimeParseException e) {
                    return true; // Let format validation handle invalid format
                }
            }, startTimeInput.textProperty(), endTimeInput.textProperty()),
            endTimeInput,
            I18n.i18nTextProperty(ProgramI18nKeys.EndTimeAfterStartTime)
        );

        // Add validation that at least one of audio or video must be offered
        validationSupport.addValidationRule(
            Bindings.createBooleanBinding(() ->
                audioCheck.isSelected() || videoCheck.isSelected(),
                audioCheck.selectedProperty(), videoCheck.selectedProperty()
            ),
            audioCheck, // Decorate the audio checkbox when invalid
            I18n.i18nTextProperty(ProgramI18nKeys.AtLeastOneMediaRequired)
        );

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(ProgramI18nKeys.CancelProgram));
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(isEdit ? SaveProgram : ProgramI18nKeys.CreateSession));

        // Bind save button disable property to validation and changes
        // Save button is disabled when:
        // 1. Form validation fails (!isValid())
        // 2. No meaningful changes detected
        //
        // Note: For NEW sessions, insertEntity() marks store as changed immediately,
        // so we can't use hasChanges() alone. Instead we check if fields have actual values.
        saveButton.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> {
                    // Disabled if validation fails
                    if (!validationSupport.isValid()) {
                        return true;
                    }

                    if (isEdit) {
                        // For edit mode: check if anything changed
                        boolean checkboxesChanged =
                            audioCheck.isSelected() != initialAudioOffered.get() ||
                            videoCheck.isSelected() != initialVideoOffered.get();

                        // Entity changes (name, startTime, endTime updates)
                        boolean entityChanged = updateStore.hasChanges();

                        // Enabled if any change detected
                        return !(entityChanged || checkboxesChanged);
                    } else {
                        // For new sessions: insertEntity() marks store as changed immediately
                        // So we just check validation - if validation passes, user can save
                        // (Validation already requires non-empty times and at least one checkbox)
                        return false; // Enabled if validation passed
                    }
                },
                // IMPORTANT: Monitor ALL properties that could trigger re-evaluation
                // When text fields change, entity updates → updateStore.hasChanges() changes
                // So we MUST listen to text properties to trigger re-evaluation
                startTimeInput.textProperty(),
                endTimeInput.textProperty(),
                storeHasChangesTrigger, // Polled every 300ms to detect updateStore changes
                audioCheck.selectedProperty(),
                videoCheck.selectedProperty(),
                initialAudioOffered,
                initialVideoOffered,
                initialSetupComplete
            )
        );

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all to dialog content
        dialogContent.getChildren().addAll(titleLabel, formFields, footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> {
            pollTask.cancel(); // Stop polling when dialog closes
            updateStore.cancelChanges();
            dialogCallback.closeDialog();
        });

        saveButton.setOnAction(e -> {
            // Validate all fields using ValidationSupport
            if (!validationSupport.isValid()) {
                return; // ValidationSupport will show the error decorations and popup
            }

            pollTask.cancel(); // Stop polling when saving
            // Save the session
            // Note: name, startTime, and endTime are already set on entityForDialog by listeners
            saveSession(isEdit, entityForDialog, audioCheck.isSelected(),
                       videoCheck.isSelected(), date, updateStore, programModel, dialogCallback, onSuccess);
        });
    }

    /**
     * Saves the session to the database.
     * Note: name, startTime, and endTime are already set on scheduledItemToSave by dynamic listeners
     */
    private static void saveSession(boolean isEdit, ScheduledItem scheduledItemToSave,
                                    boolean audioOffered, boolean videoOffered,
                                    LocalDate date, UpdateStore updateStore, ProgramModel programModel,
                                    DialogCallback dialogCallback, Runnable onSuccess) {

        // The scheduledItemToSave already has name, startTime, and endTime set by listeners
        Event event = FXEvent.getEvent();
        Site site = programModel.getProgramSite();

        if (isEdit) {
            // For edit, update audio/video child scheduledItems based on checkbox changes
            programModel.updateMediaChildScheduledItems(scheduledItemToSave, audioOffered, videoOffered, updateStore)
                .inUiThread()
                .onSuccess(x -> submitSessionChanges(updateStore, dialogCallback, onSuccess))
                .onFailure(error -> showErrorDialog(error));
        } else {
            // Set required fields for new session
            scheduledItemToSave.setEvent(event);
            scheduledItemToSave.setSite(site);
            scheduledItemToSave.setDate(date);
            scheduledItemToSave.setItem(programModel.getSessionProgramItem());

            // Link to teaching day ticket bookable scheduled item for this date
            ScheduledItem bookableScheduledItem = programModel.getTeachingsBookableScheduledItems().stream()
                .filter(si -> si.getDate().equals(date))
                .findFirst()
                .orElse(null);
            scheduledItemToSave.setBookableScheduledItem(bookableScheduledItem);

            // Create audio/video child scheduledItems using ProgramModel
            programModel.createMediaChildScheduledItems(scheduledItemToSave, audioOffered, videoOffered, updateStore);

            // Submit changes
            submitSessionChanges(updateStore, dialogCallback, onSuccess);
        }
    }

    /**
     * Submits update store changes and handles success/failure.
     */
    private static void submitSessionChanges(UpdateStore updateStore, DialogCallback dialogCallback, Runnable onSuccess) {
        Console.log("SessionDialog: Submitting changes for scheduled item");
        updateStore.submitChanges()
            .inUiThread()
            .onSuccess(result -> {
                Console.log("SessionDialog: Successfully saved session");
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            })
            .onFailure(SessionDialog::showErrorDialog);
    }

    /**
     * Shows an error dialog with the given error message.
     */
    private static void showErrorDialog(Throwable error) {
        Console.log(error);

        // Extract error message, showing a user-friendly message
        String errorMessage = error.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "An unexpected error occurred: " + error.getClass().getSimpleName();
        }

        // Create and show error dialog using the application's standard dialog system
        DialogContent dialog = DialogContent.createConfirmationDialog(
            "Error",
            "Failed to save session",
            errorMessage
        );
        dialog.setOk();
        DialogBuilderUtil.showModalNodeInGoldLayout(dialog, FXMainFrameDialogArea.getDialogArea());
        dialog.getPrimaryButton().setOnAction(a -> dialog.getDialogCallback().closeDialog());
    }

    /**
     * Checks for existing child scheduledItems with media and warns user before allowing changes.
     */
    private static void checkAndWarnForMediaDeletion(ScheduledItem scheduledItem, CheckBox audioCheck,
                                                     CheckBox videoCheck, boolean initialAudioOffered,
                                                     boolean initialVideoOffered, EntityStore entityStore) {
        // Query for audio child scheduledItems with media
        entityStore.executeQuery(
            "select id from Media where scheduledItem.programScheduledItem=$1 and type=$2",
            scheduledItem, "AUDIO"
        ).onSuccess(audioMediaList -> {
            boolean hasAudioMedia = audioMediaList.size() > 0;
            int audioMediaCount = audioMediaList.size();

            // Add listener to audio checkbox
            audioCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // If unchecking and there are existing media files
                if (initialAudioOffered && !newVal && hasAudioMedia) {
                    // Show warning confirmation dialog
                    ModalityDialog.showConfirmationDialog(
                        I18n.getI18nText(ProgramI18nKeys.DisableAudioWarning, audioMediaCount),
                        () -> {
                            // User confirmed - keep unchecked
                            // Nothing to do here, checkbox is already unchecked
                        },
                        () -> {
                            // User cancelled - revert checkbox
                            audioCheck.setSelected(true);
                        }
                    );
                }
            });
        });

        // Query for video child scheduledItems with media
        entityStore.executeQuery(
            "select id from Media where scheduledItem.programScheduledItem=$1 and (type=$2 or type=$3)",
            scheduledItem, "VOD", "LIVESTREAM"
        ).onSuccess(videoMediaList -> {
            boolean hasVideoMedia = videoMediaList.size() > 0;
            int videoMediaCount = videoMediaList.size();

            // Add listener to video checkbox
            videoCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // If unchecking and there are existing media files
                if (initialVideoOffered && !newVal && hasVideoMedia) {
                    // Show warning confirmation dialog
                    ModalityDialog.showConfirmationDialog(
                        I18n.getI18nText(ProgramI18nKeys.DisableVideoWarning, videoMediaCount),
                        () -> {
                            // User confirmed - keep unchecked
                            // Nothing to do here, checkbox is already unchecked
                        },
                        () -> {
                            // User cancelled - revert checkbox
                            videoCheck.setSelected(true);
                        }
                    );
                }
            });
        });
    }
}
