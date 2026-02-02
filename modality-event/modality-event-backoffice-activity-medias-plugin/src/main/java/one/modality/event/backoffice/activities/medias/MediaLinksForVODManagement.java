package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.scene.control.Separator;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.result.EntityChangesBuilder;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.i18n.LabelTextField;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entity.message.sender.ModalityEntityMessageSender;
import one.modality.base.client.time.BackOfficeTimeFormats;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.MediaType;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.markers.HasEndTime;
import one.modality.base.shared.entities.markers.HasStartTime;
import one.modality.base.shared.knownitems.KnownItem;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
public class MediaLinksForVODManagement extends MediaLinksManagement {

    private static final int URL_TEXT_FIELD_WITH = 600;

    private final VideoTabView parentVideoTabView;

    public MediaLinksForVODManagement(EntityStore entityStore, List<LocalDate> teachingsDates, ObservableList<ScheduledItem> scheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase, VideoTabView videoTabView) {
        super(KnownItem.VIDEO.getCode(), entityStore, teachingsDates, scheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        parentVideoTabView = videoTabView;
        mainContainer.getStyleClass().add("media-form-container");
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);

        // Separate dates into future and past
        LocalDate today = LocalDate.now();
        List<LocalDate> futureDates = new java.util.ArrayList<>();
        List<LocalDate> pastDates = new java.util.ArrayList<>();

        for (LocalDate date : teachingsDates) {
            if (date.isAfter(today) || date.equals(today)) {
                futureDates.add(date);
            } else {
                pastDates.add(date);
            }
        }

        // Add Future Sessions section
        if (!futureDates.isEmpty()) {
            teachingDatesVBox.getChildren().add(buildDatesSectionSeparator(MediasI18nKeys.FutureSessions));
            futureDates.forEach(date -> teachingDatesVBox.getChildren().add(computeTeachingDateLine(date)));
        }

        // Add Past Sessions section
        if (!pastDates.isEmpty()) {
            teachingDatesVBox.getChildren().add(buildDatesSectionSeparator(MediasI18nKeys.PastSessions));
            pastDates.forEach(date -> teachingDatesVBox.getChildren().add(computeTeachingDateLine(date)));
        }
    }

    protected VBox buildDatesSectionSeparator(Object i18nKey) {
        VBox separatorBox = new VBox(10);
        separatorBox.setPadding(new Insets(20, 20, 10, 20));

        Label sectionLabel = I18nControls.newLabel(i18nKey);
        sectionLabel.getStyleClass().addAll(Bootstrap.H5, Bootstrap.TEXT_SECONDARY);

        Separator separator = new Separator();

        separatorBox.getChildren().addAll(sectionLabel, separator);
        return separatorBox;
    }

    protected BorderPane computeTeachingDateLine(LocalDate date) {
        MediaLinksForVODPerDateManagement mediaLinksPerDateManagement = new MediaLinksForVODPerDateManagement(date);
        return mediaLinksPerDateManagement.drawPanel();
    }


    protected class MediaLinksForVODPerDateManagement extends MediaLinksPerDateManagement {

        protected MediaLinksForVODPerDateManagement(LocalDate date) {
            super(date);
        }

        protected BorderPane drawPanel() {
            filteredListForCurrentDay = scheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate))
                .collect(Collectors.toList());
            workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().getCode().equals(currentItemCode))
                .collect(Collectors.toList()));

            BorderPane container = new BorderPane();
            container.setBackground(Background.fill(Color.WHITE));

            VBox centerVBox = new VBox();
            container.setCenter(centerVBox);
            centerVBox.setVisible(false);
            centerVBox.setManaged(false);
            //We create a separate method for building the header line because it will likely be the same for the child classes, but the content below will change
            HBox headerLine = buildHeaderLine(centerVBox);

            container.setTop(headerLine);
            Separator separator = new Separator();
            centerVBox.getChildren().add(separator);

            /* The content with the list of the teachings per day and the links **/
            List<ScheduledItem> filteredAndSortedList = scheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate)) // Filter by date
                .sorted(Comparator.comparing(item -> {
                    ScheduledItem programScheduledItem = item.getProgramScheduledItem();
                    // Priority: first check scheduledItem's own startTime, then timeline's startTime
                    LocalTime startTime = programScheduledItem.getStartTime();
                    if (startTime == null) {
                        Timeline timeline = programScheduledItem.getTimeline();
                        if (timeline != null) {
                            startTime = timeline.getStartTime();
                        }
                    }
                    return startTime;
                }, Comparator.nullsLast(Comparator.naturalOrder()))) // Sort by start date, nulls last
                .collect(Collectors.toList());

            for (ScheduledItem currentVideoScheduledItem : filteredAndSortedList) {
                buildVideoTeachingContainer(currentVideoScheduledItem, centerVBox);
            }

            container.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1)
            )));
            return container;
        }

        private void buildVideoTeachingContainer(ScheduledItem currentVideoScheduledItem, VBox centerVBox) {
                UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
                //We add in the parentView the updateStore.hasChangedProperty so we know if we can or not change the edited event without forgetting the current local changes
                //made here
                parentVideoTabView.addUpdateStoreHasChangesProperty(EntityBindings.hasChangesProperty(localUpdateStore));

                ScheduledItem workingCurrentVideoScheduledItem = localUpdateStore.updateEntity(currentVideoScheduledItem);
                //First of all, we read the Video ScheduledItems linked to the teachings
                /* Here we create the container for each teaching **/
                VBox currentVBox = new VBox(15);
                currentVBox.setPadding(new Insets(20, 20, 20, 40));

                // First line: Time - Title
                String name = workingCurrentVideoScheduledItem.getProgramScheduledItem().getName();
                if (name == null) name = "Unknown";
                Timeline timeline = workingCurrentVideoScheduledItem.getProgramScheduledItem().getTimeline();
                HasStartTime startTimeHolder;
                HasEndTime endTimeHolder;
                if (timeline!= null) {
                    //Case of Festivals
                    startTimeHolder = timeline;
                    endTimeHolder = timeline;
                } else {
                    //Case of recurring events
                    startTimeHolder = workingCurrentVideoScheduledItem.getProgramScheduledItem();
                    endTimeHolder = workingCurrentVideoScheduledItem.getProgramScheduledItem();
                }
                Label startTimeLabel = I18nControls.newLabel("{0} - {1}",
                    LocalizedTime.formatLocalTimeProperty(startTimeHolder.getStartTime(), BackOfficeTimeFormats.MEDIA_TIME_FORMAT),
                    LocalizedTime.formatLocalTimeProperty(endTimeHolder.getEndTime(), BackOfficeTimeFormats.MEDIA_TIME_FORMAT)
                );
                startTimeLabel.getStyleClass().add(Bootstrap.STRONG);
                Label teachingTitle = new Label(name);
                teachingTitle.getStyleClass().add(Bootstrap.STRONG);

                HBox titleLine = new HBox(15, startTimeLabel, teachingTitle);
                titleLine.setAlignment(Pos.CENTER_LEFT);
                currentVBox.getChildren().add(titleLine);


                // Links section with label
                Label linksLabel = I18nControls.newLabel(MediasI18nKeys.VODLinks);
                Label noLinkLabel = I18nControls.newLabel(MediasI18nKeys.NoLinkDefinedYet);
                HBox linkLabelHBox = new HBox(10, linksLabel, noLinkLabel);
                currentVBox.getChildren().add(linkLabelHBox);

                /* ********LINKS BOX MANAGEMENT ****************** */
                /* *********************************************** */
                //We look if there are existing medias for this teaching
                //For VOD, we can have several medias since the video can contain several part if the livestreamed has been interrupted to due techincal pb
                VBox mediasListVBox = new VBox();
                mediasListVBox.setAlignment(Pos.CENTER_LEFT);
                mediasListVBox.setSpacing(10);
                currentVBox.getChildren().add(mediasListVBox);

                ObservableList<Media> mediaList = FXCollections.observableArrayList(
                    workingMedias.stream().map(localUpdateStore::updateEntity)
                        .filter(media -> media.getScheduledItem().equals(workingCurrentVideoScheduledItem))
                        .collect(Collectors.toList())
                );

                noLinkLabel.visibleProperty().bind(Bindings.isEmpty(mediaList));

                Switch publishedSwitch = new Switch();
                // Bind the children of mediasListVBox to mediaList
                ObservableLists.bindConverted(mediasListVBox.getChildren(), mediaList, media -> {
                    VBox mediaNode = drawMediaLinkContainer(media, mediaList, localUpdateStore,publishedSwitch);
                    mediaNode.setUserData(media);// Set user data for removal reference
                    return mediaNode;
                });

                // Add a listener to manage the removals. The addition are managed by the bindConverted just above
                mediaList.addListener((ListChangeListener<Media>) change -> {
                    while (change.next()) {
                        if (change.wasRemoved()) {
                            for (Media removedMedia : change.getRemoved()) {
                                mediasListVBox.getChildren().removeIf(node ->
                                    node.getUserData() != null && node.getUserData().equals(removedMedia)
                                );
                                localUpdateStore.deleteEntity(removedMedia);
                            }
                        }
                    }
                });

                HBox addMediaHBox = new HBox();
                addMediaHBox.setAlignment(Pos.CENTER_LEFT);
                addMediaHBox.setSpacing(20);

                Label addLinkLabel = Bootstrap.small(I18nControls.newLabel(MediasI18nKeys.AddLink));
                TextTheme.createSecondaryTextFacet(addLinkLabel).style();

                SVGPath addIcon = SvgIcons.setSVGPathFill(SvgIcons.createPlusPath(), Color.web("#0096D6"));
                MonoPane addButton = SvgIcons.createButtonPane(addIcon, () -> {
                    Media createdMedia = localUpdateStore.insertEntity(Media.class);
                    createdMedia.setScheduledItem(workingCurrentVideoScheduledItem);
                    createdMedia.setType(MediaType.VOD);
                    if (!mediaList.isEmpty() && mediaList.get(mediaList.size() - 1).getOrd() != null) {
                        createdMedia.setOrd(mediaList.get(mediaList.size() - 1).getOrd() + 1);
                    } else {
                        createdMedia.setOrd(0);
                    }
                    //By adding the media to mediaList, the drawMediaLinkContainer will be called because of the bondage between mediaList and the VBox children
                    mediaList.add(createdMedia);
                });

                addMediaHBox.getChildren().addAll(addLinkLabel, addButton);
                currentVBox.getChildren().add(addMediaHBox);

                Label publicationDelayedLabel = I18nControls.newLabel(MediasI18nKeys.VODDelayed);
                Switch publicationDelayedSwitch = new Switch();
                if (workingCurrentVideoScheduledItem.isVodDelayed() != null)
                    publicationDelayedSwitch.setSelected(workingCurrentVideoScheduledItem.isVodDelayed());

                HBox VODPublicationDelayeddHBox = new HBox(10, publicationDelayedLabel, publicationDelayedSwitch);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label publishedLabel = I18nControls.newLabel(MediasI18nKeys.VODPublished);

                publishedSwitch.setSelected(workingCurrentVideoScheduledItem.isPublished());
                publishedSwitch.selectedProperty().addListener(observable -> {
                    //Here we update all the media
                    workingCurrentVideoScheduledItem.setPublished(publishedSwitch.selectedProperty().get());
                    //We remove the published value if delay is selected
                    if (publishedSwitch.selectedProperty().get())
                        publicationDelayedSwitch.selectedProperty().setValue(false);
                });

                publicationDelayedSwitch.selectedProperty().addListener(observable -> {
                    //Here we update all the media
                    workingCurrentVideoScheduledItem.setVodDelayed(publicationDelayedSwitch.selectedProperty().get());
                    //We remove the published value if delay is selected
                    if (publicationDelayedSwitch.selectedProperty().get())
                        publishedSwitch.selectedProperty().setValue(false);
                });

                HBox VODPublisheddHBox = new HBox(10, publishedLabel, publishedSwitch);
                VODPublisheddHBox.visibleProperty().bind(Bindings.isEmpty(mediaList).not());
                // Add a listener to mediaList to handle empty state
                mediaList.addListener((ListChangeListener<Object>) change -> {
                    if (mediaList.isEmpty()) {
                        workingCurrentVideoScheduledItem.setPublished(false);
                        publishedSwitch.setSelected(false); // Ensure the switch reflects the updated value
                    }
                });

                HBox publicationInfoHBox = new HBox(VODPublicationDelayeddHBox, spacer, VODPublisheddHBox);
                publicationInfoHBox.getStyleClass().add("publication-info-container");
                publicationInfoHBox.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(publicationInfoHBox);

                // Override name section
                Label overrideNameLabel = new Label("Override Name");
                Switch overrideNameSwitch = new Switch();
                HBox overrideNameHeader = new HBox(15, overrideNameLabel, overrideNameSwitch);
                overrideNameHeader.setAlignment(Pos.CENTER_LEFT);
                currentVBox.getChildren().add(overrideNameHeader);

                // Name field (only visible when override is enabled)
                Label nameLabel = new Label(I18n.getI18nText("Name"));
                TextField nameTextField = new TextField();
                nameTextField.setPromptText(I18n.getI18nText("Name"));
                if (workingCurrentVideoScheduledItem.getName() == null) {
                    nameTextField.setPromptText(I18n.getI18nText(workingCurrentVideoScheduledItem.getProgramScheduledItem().getName()));
                } else {
                    overrideNameSwitch.setSelected(true);
                    nameTextField.setText(workingCurrentVideoScheduledItem.getName());
                }
                nameTextField.getStyleClass().add("media-name-textfield");
                nameTextField.setMaxWidth(Double.MAX_VALUE);

                VBox nameBox = new VBox(8, nameLabel, nameTextField);
                nameBox.visibleProperty().bind(overrideNameSwitch.selectedProperty());
                nameBox.managedProperty().bind(nameBox.visibleProperty());
                currentVBox.getChildren().add(nameBox);

                // Clear the TextField's value when the Switch is turned off
                overrideNameSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        nameTextField.clear();
                    } else {
                        nameTextField.setPromptText(I18n.getI18nText(workingCurrentVideoScheduledItem.getProgramScheduledItem().getName()));
                    }
                });

                nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue.isEmpty()) {
                        workingCurrentVideoScheduledItem.setName(null);
                    } else {
                        workingCurrentVideoScheduledItem.setName(nameTextField.getText());
                    }
                });

                Label commentLabel = I18nControls.newLabel(MediasI18nKeys.VODComment);
                commentLabel.setPadding(new Insets(10, 0, 0, 0));
                currentVBox.getChildren().add(commentLabel);

//                TextField commentTextField = new TextField();
//                commentTextField.setPromptText(I18n.getI18nText(MediasI18nKeys.VODPromptComment));
//                currentVBox.getChildren().add(commentTextField);
//                commentTextField.setMaxWidth(URL_TEXT_FIELD_WITH);
//                if (workingCurrentVideoScheduledItem.getComment() != null)
//                    commentTextField.setText(workingCurrentVideoScheduledItem.getComment());
//                commentTextField.textProperty().addListener(observable -> workingCurrentVideoScheduledItem.setComment(commentTextField.getText()));
                LabelTextField commentTextField = new LabelTextField(workingCurrentVideoScheduledItem,"comment","commentLabel",localUpdateStore);
                commentTextField.getView().getStyleClass().add("publication-info-container");
                currentVBox.getChildren().add(commentTextField.getView());

                // Custom expiration date section
                Label customExpirationDate = I18nControls.newLabel(MediasI18nKeys.VODCustomExpirationDate);
                Switch customExpirationDateSwitch = new Switch();
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null) {
                    customExpirationDateSwitch.setSelected(true);
                }
                HBox VODCustomExpirationHBox = new HBox(15, customExpirationDate, customExpirationDateSwitch);
                VODCustomExpirationHBox.setAlignment(Pos.CENTER_LEFT);
                currentVBox.getChildren().add(VODCustomExpirationHBox);

                // Available until fields (only visible when custom expiration is enabled)
                DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter(BackOfficeTimeFormats.MEDIA_DATE_FORMAT);
                DateTimeFormatter timeFormatter = LocalizedTime.timeFormatter(BackOfficeTimeFormats.MEDIA_TIME_FORMAT);

                Label contentAvailableUntilLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntil);

                TextField availableUntilDateTextField = new TextField();
                availableUntilDateTextField.setPromptText(LocalDate.of(2027, 10, 25).format(dateFormatter));
                availableUntilDateTextField.setMaxWidth(100);
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilDateTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(dateFormatter));
                validationSupport.addDateValidation(availableUntilDateTextField, dateFormatter, availableUntilDateTextField, I18n.i18nTextProperty("ValidationDateFormatIncorrect"));

                TextField availableUntilTimeTextField = new TextField();
                availableUntilTimeTextField.setPromptText("18:25");
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilTimeTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(timeFormatter));
                availableUntilTimeTextField.setMaxWidth(50);
                validationSupport.addDateValidation(availableUntilTimeTextField, timeFormatter, availableUntilTimeTextField, I18n.i18nTextProperty("ValidationTimeFormatIncorrect"));

                HBox dateTimeFieldsBox = new HBox(15, availableUntilDateTextField, availableUntilTimeTextField);
                dateTimeFieldsBox.setAlignment(Pos.CENTER_LEFT);

                VBox availableUntilBox = new VBox(8, contentAvailableUntilLabel, dateTimeFieldsBox);
                availableUntilBox.visibleProperty().bind(customExpirationDateSwitch.selectedProperty());
                availableUntilBox.managedProperty().bind(availableUntilBox.visibleProperty());
                currentVBox.getChildren().add(availableUntilBox);

                availableUntilDateTextField.textProperty().addListener(observable -> {
                    try {
                        LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                        LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                        workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                    } catch (DateTimeParseException ignored) {
                    }
                });

                availableUntilTimeTextField.textProperty().addListener(observable -> {
                    try {
                        LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                        LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                        workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                    } catch (DateTimeParseException ignored) {
                    }
                });

                customExpirationDateSwitch.selectedProperty().addListener(observable -> {
                    if (!customExpirationDateSwitch.selectedProperty().get()) {
                        workingCurrentVideoScheduledItem.setExpirationDate(null);
                    } else {
                        try {
                            LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                            LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                            workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                        } catch (DateTimeParseException ignored) {
                        }
                    }
                });

                // Save button at the bottom
                Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Save));
                saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(localUpdateStore).not());
                HBox saveButtonContainer = new HBox(saveButton);
                saveButtonContainer.setAlignment(Pos.CENTER_RIGHT);
                saveButtonContainer.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(saveButtonContainer);

                // Add the teaching container to the center VBox
                centerVBox.getChildren().add(currentVBox);

                //The action on the save button
                saveButton.setOnAction(e -> {
                    if (validationSupport.isValid()) {
                        localUpdateStore.submitChanges()
                            .onFailure(Console::error)
                            .inUiThread()
                            .onSuccess(result -> {
                                // Notifying the front-office clients of the possible changes made on ScheduledItems.published
                                ModalityEntityMessageSender.getFrontOfficeEntityMessageSender().publishEntityChanges(
                                    EntityChangesBuilder.create()
                                        .addFilteredEntityChanges(result.getCommittedChanges(), ScheduledItem.class, ScheduledItem.published)
                                        .build()
                                );
                                resetUpdateStoreAndOtherComponents();
                            });
                    }
                });

                //We submit the changes in the update store to add the child scheduledItem that could have been created
                if (localUpdateStore.hasChanges()) {
                    localUpdateStore.submitChanges().onFailure(Console::error)
                        .onSuccess(Console::log);
                }

                Separator hSeparator = new Separator();
                hSeparator.setOrientation(Orientation.HORIZONTAL);
                hSeparator.setPadding(new Insets(30, 0, 30, 0));
                centerVBox.getChildren().add(hSeparator);
        }

        private VBox drawMediaLinkContainer(Media currentMedia, ObservableList<Media> mediaList, UpdateStore localUpdateStore,Switch publishSwitch) {
            VBox vBoxToReturn = new VBox(8);

            // Link label
            Label linkLabel = new Label(I18n.getI18nText("Link"));

            // Link text field and remove button in HBox
            TextField linkTextField = new TextField();
            linkTextField.promptTextProperty().bind(I18n.i18nTextProperty(MediasI18nKeys.EnterLinkHere));
            linkTextField.getStyleClass().add("vod-link-textfield");
            linkTextField.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(linkTextField, Priority.ALWAYS);
            if (currentMedia != null) {
                linkTextField.setText(currentMedia.getUrl());
            }
            validationSupport.addUrlValidation(linkTextField, linkTextField, I18n.i18nTextProperty(MediasI18nKeys.MalformedUrl));

            // We update the value of the media according to the text field
            linkTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                assert currentMedia != null : "media should be not null";
                currentMedia.setUrl(newValue); // Update the URL of the current media
                // Mark as not published if URL is invalid
                currentMedia.getScheduledItem().setPublished(isValidURL(newValue));
                publishSwitch.setSelected(isValidURL(newValue));
            });

            SVGPath removeIcon = Bootstrap.textDanger(SvgIcons.createMinusPath());
            MonoPane removeButton = SvgIcons.createButtonPane(removeIcon, () -> {
                localUpdateStore.deleteEntity(currentMedia);
                mediaList.remove(currentMedia);
            });

            HBox linkFieldBox = new HBox(15, linkTextField, removeButton);
            linkFieldBox.setAlignment(Pos.CENTER_LEFT);
            linkFieldBox.getStyleClass().add("media-field-row");

            vBoxToReturn.getChildren().addAll(linkLabel, linkFieldBox);

            return vBoxToReturn;
        }

        public boolean isValidURL(String url) {
            try {
                new URL(url); // Try creating a URL object
                return true;  // If no exception, it's a valid URL
            } catch (Exception e) {
                return false; // If exception, it's not a valid URL
            }
        }
    }
}
