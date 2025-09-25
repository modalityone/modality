package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.layout.Layouts;
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
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.i18n.LabelTextField;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.messaging.ModalityMessaging;
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
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        teachingsDates.forEach(date -> teachingDatesVBox.getChildren().add(computeTeachingDateLine(date)));
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
            List<ScheduledItem> filteredListForCurrentDay = scheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate)) // Filter by date
                .sorted(Comparator.comparing(item -> item.getProgramScheduledItem().getTimeline().getStartTime())) // Sort by start date
                .collect(Collectors.toList());

            for (ScheduledItem currentVideoScheduledItem : filteredListForCurrentDay) {
                UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
                //We add in the parentView the updateStore.hasChangedProperty so we know if we can or not change the edited event without forgetting the current local changes
                //made here
                parentVideoTabView.addUpdateStoreHasChangesProperty(EntityBindings.hasChangesProperty(localUpdateStore));

                ScheduledItem workingCurrentVideoScheduledItem = localUpdateStore.updateEntity(currentVideoScheduledItem);
                //First of all, we read the Video ScheduledItems linked to the teachings
                /* Here we create the line for each teaching **/
                VBox currentVBox = new VBox();

                currentVBox.setSpacing(15);
                currentVBox.setPadding(new Insets(20, 20, 20, 40));
                currentVBox.setAlignment(Pos.CENTER_LEFT);

                HBox firstLine = new HBox();
                firstLine.setAlignment(Pos.CENTER_RIGHT);
                VBox teachingInfoVBox = new VBox();
                Region firstSpacer = new Region();
                HBox.setHgrow(firstSpacer, Priority.ALWAYS);
                firstLine.getChildren().addAll(teachingInfoVBox, firstSpacer);

                String name = workingCurrentVideoScheduledItem.getProgramScheduledItem().getName();
                if (name == null) name = "Unknown";
                Label teachingTitle = new Label(name);
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
                teachingTitle.getStyleClass().add(Bootstrap.STRONG);
                startTimeLabel.getStyleClass().add(Bootstrap.STRONG);

                Button saveButton = Bootstrap.largeSuccessButton(I18nControls.newButton(BaseI18nKeys.Save));
                saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(localUpdateStore).not());

                HBox saveButtonContainer = new HBox(saveButton);
                saveButtonContainer.setAlignment(Pos.CENTER_RIGHT);
                saveButtonContainer.setPadding(new Insets(0, 40, 0, 0));
                firstLine.getChildren().add(saveButtonContainer);

                teachingInfoVBox.getChildren().addAll(teachingTitle, startTimeLabel);
                currentVBox.getChildren().add(firstLine);


                Label linksLabel = I18nControls.newLabel(MediasI18nKeys.VODLinks);
                Label noLinkLabel = I18nControls.newLabel(MediasI18nKeys.NoLinkDefinedYet);
                HBox linkLabelHBox = new HBox(linksLabel, noLinkLabel);
                linkLabelHBox.setSpacing(10);
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
                    HBox mediaNode = drawMediaLinkContainer(media, mediaList, localUpdateStore,publishedSwitch);
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
                publicationInfoHBox.setMaxWidth(URL_TEXT_FIELD_WITH);
                publicationInfoHBox.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(publicationInfoHBox);

                Label overrideNameLabel = new Label("Override Name");
                Switch overrideNameSwitch = new Switch();

                TextField nameTextField = new TextField();
                nameTextField.setPromptText(I18n.getI18nText("Name"));
                if (workingCurrentVideoScheduledItem.getName() == null) {
                    nameTextField.setPromptText(I18n.getI18nText(workingCurrentVideoScheduledItem.getProgramScheduledItem().getName()));
                } else {
                    overrideNameSwitch.setSelected(true);
                    nameTextField.setText(workingCurrentVideoScheduledItem.getName());
                }
                nameTextField.setPrefWidth(435);

                // Bind the properties of the TextField to the Switch
                nameTextField.editableProperty().bind(overrideNameSwitch.selectedProperty());
                nameTextField.disableProperty().bind(overrideNameSwitch.selectedProperty().not());

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

                HBox overrideLine = new HBox(overrideNameLabel, overrideNameSwitch, nameTextField);
                overrideLine.setSpacing(10);
                overrideLine.setAlignment(Pos.CENTER_LEFT);
                overrideLine.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(overrideLine);

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
                commentTextField.setMaxWidth(URL_TEXT_FIELD_WITH);
                currentVBox.getChildren().add(commentTextField.getView());

                HBox customContentAvailableUntilHBox = new HBox();
                customContentAvailableUntilHBox.setPadding(new Insets(20, 0, 0, 0));
                customContentAvailableUntilHBox.setMaxWidth(URL_TEXT_FIELD_WITH);

                Label customExpirationDate = I18nControls.newLabel(MediasI18nKeys.VODCustomExpirationDate);
                Switch customExpirationDateSwitch = new Switch();
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null) {
                    customExpirationDateSwitch.setSelected(true);
                }
                HBox VODCustomExpirationHBox = new HBox(10, customExpirationDate, customExpirationDateSwitch);
                customContentAvailableUntilHBox.setAlignment(Pos.CENTER_LEFT);

                Region spacer2 = new Region();
                HBox.setHgrow(spacer2, Priority.SOMETIMES);
                customContentAvailableUntilHBox.getChildren().addAll(VODCustomExpirationHBox, spacer2);

                HBox rightHBox = new HBox();
                rightHBox.setAlignment(Pos.CENTER_RIGHT);
                Layouts.bindManagedAndVisiblePropertiesTo(customExpirationDateSwitch.selectedProperty(), rightHBox);

                Label contentAvailableUntilLabel = I18nControls.newLabel(MediasI18nKeys.AvailableUntil);
                rightHBox.getChildren().add(contentAvailableUntilLabel);

                DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter(BackOfficeTimeFormats.MEDIA_DATE_FORMAT);
                DateTimeFormatter timeFormatter = LocalizedTime.timeFormatter(BackOfficeTimeFormats.MEDIA_TIME_FORMAT);

                TextField availableUntilDateTextField = new TextField();
                availableUntilDateTextField.setPromptText(LocalDate.of(2027, 10, 25).format(dateFormatter));
                availableUntilDateTextField.setMaxWidth(100);
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilDateTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(dateFormatter));


                HBox.setMargin(availableUntilDateTextField, new Insets(0, 15, 0, 25));
                rightHBox.getChildren().add(availableUntilDateTextField);
                validationSupport.addDateValidation(availableUntilDateTextField, dateFormatter, availableUntilDateTextField, I18n.i18nTextProperty("ValidationDateFormatIncorrect")); // ???

                TextField availableUntilTimeTextField = new TextField();
                availableUntilTimeTextField.setPromptText("18:25");
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilTimeTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(timeFormatter));

                availableUntilTimeTextField.setMaxWidth(50);
                rightHBox.getChildren().add(availableUntilTimeTextField);
                validationSupport.addDateValidation(availableUntilTimeTextField, timeFormatter, availableUntilTimeTextField, I18n.i18nTextProperty("ValidationTimeFormatIncorrect")); // ???

                availableUntilDateTextField.textProperty().addListener(observable -> {
                    try {
                        LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                        LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                        // Combine the date and time to create LocalDateTime
                        workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                    } catch (DateTimeParseException ignored) {
                    }
                });

                availableUntilTimeTextField.textProperty().addListener(observable -> {
                    try {
                        LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                        LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                        // Combine the date and time to create LocalDateTime
                        workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                    } catch (DateTimeParseException ignored) {
                    }
                });

                customContentAvailableUntilHBox.getChildren().add(rightHBox);
                customExpirationDateSwitch.selectedProperty().addListener(observable -> {
                    if (!customExpirationDateSwitch.selectedProperty().get()) {
                        workingCurrentVideoScheduledItem.setExpirationDate(null);
                    } else {
                        try {
                            LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                            LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                            // Combine the date and time to create LocalDateTime
                            workingCurrentVideoScheduledItem.setExpirationDate(LocalDateTime.of(date, time));
                        } catch (DateTimeParseException ignored) {
                        }
                    }
                });

                currentVBox.getChildren().add(customContentAvailableUntilHBox);

                currentVBox.setSpacing(10);
                centerVBox.getChildren().add(currentVBox);

                Separator hSeparator = new Separator();
                hSeparator.setOrientation(Orientation.HORIZONTAL);
                hSeparator.setPadding(new Insets(30, 0, 30, 0));
                centerVBox.getChildren().add(hSeparator);

                //The action on the save button
                saveButton.setOnAction(e -> {
                    if (validationSupport.isValid()) {
                        localUpdateStore.submitChanges()
                            .onFailure(Console::log)
                            .inUiThread()
                            .onSuccess(result -> {
                                // Notifying the front-office clients of the possible changes made on ScheduledItems.published
                                ModalityMessaging.getFrontOfficeEntityMessaging().publishEntityChanges(
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
                    localUpdateStore.submitChanges().onFailure(Console::log)
                        .onSuccess(Console::log);
                }
            }
            /* ***************** END LINK BOX MANAGEMENT *********************** */

            container.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1)
            )));
            return container;
        }

        private HBox drawMediaLinkContainer(Media currentMedia, ObservableList<Media> mediaList, UpdateStore localUpdateStore,Switch publishSwitch) {
            HBox hBoxToReturn = new HBox();
            hBoxToReturn.setSpacing(20);

            TextField linkTextField = new TextField();
            linkTextField.promptTextProperty().bind(I18n.i18nTextProperty(MediasI18nKeys.EnterLinkHere));
            linkTextField.setMinWidth(URL_TEXT_FIELD_WITH);
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

            hBoxToReturn.getChildren().add(linkTextField);

            SVGPath removeIcon = Bootstrap.textDanger(SvgIcons.createMinusPath());
            MonoPane removeButton = SvgIcons.createButtonPane(removeIcon, () -> {
                localUpdateStore.deleteEntity(currentMedia);
                mediaList.remove(currentMedia);
            });

            hBoxToReturn.setAlignment(Pos.CENTER_LEFT);
            hBoxToReturn.getChildren().add(removeButton);

            return hBoxToReturn;
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
