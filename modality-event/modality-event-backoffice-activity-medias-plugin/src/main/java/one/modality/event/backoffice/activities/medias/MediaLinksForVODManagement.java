package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MediaLinksForVODManagement extends MediaLinksManagement {
    private final int urlTextFieldWith = 600;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MediaLinksForVODManagement(Item vodItem, EntityStore entityStore, ObservableList<LocalDate> teachingsDates, ObservableList<ScheduledItem> scheduledItemsReadFromDatabase, ObservableList<Media> recordingsMediasReadFromDatabase) {
        super(KnownItem.VIDEO.getCode(), entityStore, teachingsDates, scheduledItemsReadFromDatabase, recordingsMediasReadFromDatabase);
        mainContainer.setMinWidth(800);
        VBox teachingDatesVBox = new VBox();
        teachingDatesVBox.setSpacing(30);
        mainContainer.setCenter(teachingDatesVBox);
        ObservableLists.bindConverted(teachingDatesVBox.getChildren(), teachingsDates, this::computeTeachingDateLine);
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
            //We make an array which only one boolean instead of a boolean, so we can use it in lambda expression
            final boolean[] isCenterVisible = {false};

            VBox centerVBox = new VBox();
            container.setCenter(centerVBox);
            centerVBox.setVisible(false);
            centerVBox.setManaged(false);
            //We create a separate method for building the headerline because it will likely be the same for the child classes, but the content below will change
            HBox headerLine = buildHeaderLine(centerVBox, isCenterVisible);

            container.setTop(headerLine);
            Separator separator = new Separator();
            centerVBox.getChildren().add(separator);

            /* The content with the list of the teachings per day and the links **/
            List<ScheduledItem> filteredListForCurrentDay = scheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate)) // Filter by date
                .sorted(Comparator.comparing(item -> item.getParent().getTimeline().getStartTime())) // Sort by start date
                .collect(Collectors.toList());


            for (ScheduledItem currentVideoScheduledItem : filteredListForCurrentDay) {
                UpdateStore localUpdateStore = UpdateStore.createAbove(entityStore);
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

                String name = workingCurrentVideoScheduledItem.getParent().getTimeline().getName();
                if (name == null) name = "Unknown";
                Label teachingTitle = new Label(name);
                Label startTimeLabel = new Label(workingCurrentVideoScheduledItem.getParent().getTimeline().getStartTime().format(timeFormatter) + " - " + workingCurrentVideoScheduledItem.getParent().getTimeline().getEndTime().format(timeFormatter));
                teachingTitle.getStyleClass().add(Bootstrap.STRONG);
                startTimeLabel.getStyleClass().add(Bootstrap.STRONG);

                Button saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Save"));
                saveButton.disableProperty().bind(localUpdateStore.hasChangesProperty().not());

                HBox saveButtonContainer = new HBox(saveButton);
                saveButtonContainer.setAlignment(Pos.CENTER_RIGHT);
                saveButtonContainer.setPadding(new Insets(0, 40, 0, 0));
                firstLine.getChildren().add(saveButtonContainer);

                teachingInfoVBox.getChildren().addAll(teachingTitle, startTimeLabel);
                currentVBox.getChildren().add(firstLine);


                Label linksLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.VODLinks);
                Label noLinkLabel = I18nControls.bindI18nProperties(new Label(), MediasI18nKeys.NoLinkDefinedYet);
                HBox linkLabelHBox = new HBox(linksLabel,noLinkLabel);
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
                //We create an array to be able to use it in lambda expression
                //This contains the scheduledItem that contains one of several medias for this teaching
                final ScheduledItem[] currentVODScheduledItem = new ScheduledItem[1];

                ObservableList<Media> mediaList = FXCollections.observableArrayList(
                    workingMedias.stream().map(localUpdateStore::updateEntity)
                        .filter(media -> media.getScheduledItem().equals(workingCurrentVideoScheduledItem))
                        .collect(Collectors.toList())
                );

                noLinkLabel.visibleProperty().bind(Bindings.isEmpty(mediaList));

                // Bind the children of mediasListVBox to mediaList
                ObservableLists.bindConverted(mediasListVBox.getChildren(), mediaList, media -> {
                    HBox mediaNode = drawMediaLinkContainer(media, mediaList, localUpdateStore);
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
                Label addLinkLabel = I18nControls.bindI18nProperties(new Label(), "AddLink");
                addLinkLabel.getStyleClass().add(Bootstrap.SMALL);
                TextTheme.createSecondaryTextFacet(addLinkLabel).style();
                SVGPath addButton = SvgIcons.createPlusPath();
                addButton.setFill(Color.web("#0096D6"));
                MonoPane addButtonPane = new MonoPane(addButton);
                addButtonPane.setCursor(Cursor.HAND);
                addMediaHBox.getChildren().addAll(addLinkLabel, addButtonPane);
                addButtonPane.setOnMouseClicked(event -> {
                    Media createdMedia = localUpdateStore.insertEntity(Media.class);
                    createdMedia.setScheduledItem(workingCurrentVideoScheduledItem);
                    createdMedia.setType(MediaType.VOD);
                    createdMedia.setOrd(mediaList.get(mediaList.size()-1).getOrd()+1);
                    //By adding the media to mediaList, the drawMediaLinkContainer will be called because of the bondage between mediaList and the VBox children
                    mediaList.add(createdMedia);
                });
                currentVBox.getChildren().add(addMediaHBox);

                Label publicationDelayedLabel = I18nControls.bindI18nProperties(new Label(), "VODDelayed");
                Switch publicationDelayedSwitch = new Switch();
                if (workingCurrentVideoScheduledItem.isVodDelayed() != null)
                    publicationDelayedSwitch.setSelected(workingCurrentVideoScheduledItem.isVodDelayed());

                HBox VODPublicationDelayeddHBox = new HBox(10, publicationDelayedLabel, publicationDelayedSwitch);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label publishedLabel = I18nControls.bindI18nProperties(new Label(), "VODPublished");
                Switch publishedSwitch = new Switch();
                publishedSwitch.selectedProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        //Here we update all the media
                        mediaList.forEach(media -> media.setPublished(publishedSwitch.isSelected()));
                        if (publishedSwitch.selectedProperty().get())
                            publicationDelayedSwitch.selectedProperty().setValue(false);
                    }
                });

                publicationDelayedSwitch.selectedProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        //Here we update all the media
                        workingCurrentVideoScheduledItem.setVodDelayed(publicationDelayedSwitch.selectedProperty().get());
                        //We remove the published value if delay is selected
                        if (publicationDelayedSwitch.selectedProperty().get())
                            publishedSwitch.selectedProperty().setValue(false);
                    }
                });


                HBox VODPublisheddHBox = new HBox(10, publishedLabel, publishedSwitch);
                VODPublisheddHBox.visibleProperty().bind(Bindings.isEmpty(mediaList).not());

                HBox publicationInfoHBox = new HBox(VODPublicationDelayeddHBox, spacer, VODPublisheddHBox);
                publicationInfoHBox.setMaxWidth(urlTextFieldWith);
                publicationInfoHBox.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(publicationInfoHBox);

                Label commentLabel = I18nControls.bindI18nProperties(new Label(), "VODComment");
                commentLabel.setPadding(new Insets(20, 0, 0, 0));
                currentVBox.getChildren().add(commentLabel);

                TextField commentTextField = new TextField();
                commentTextField.setPromptText(I18n.getI18nText("VODPromptComment"));
                currentVBox.getChildren().add(commentTextField);
                commentTextField.setMaxWidth(urlTextFieldWith);
                if (workingCurrentVideoScheduledItem.getComment() != null)
                    commentTextField.setText(workingCurrentVideoScheduledItem.getComment());
                commentTextField.textProperty().addListener(observable -> {
                    workingCurrentVideoScheduledItem.setComment(commentTextField.getText());
                });

                HBox customContentAvailableUntilHBox = new HBox();
                customContentAvailableUntilHBox.setPadding(new Insets(20, 0, 0, 0));
                customContentAvailableUntilHBox.setMaxWidth(urlTextFieldWith);

                Label customExpirationDate = I18nControls.bindI18nProperties(new Label(), "VODCustomExpirationDate");
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
                rightHBox.visibleProperty().bind(customExpirationDateSwitch.selectedProperty());
                rightHBox.managedProperty().bind(customExpirationDateSwitch.selectedProperty());

                Label contentAvailableUntilLabel = I18nControls.bindI18nProperties(new Label(), "AvailableUntil");
                rightHBox.getChildren().add(contentAvailableUntilLabel);

                TextField availableUntilDateTextField = new TextField();
                availableUntilDateTextField.setPromptText("25-10-2027");
                availableUntilDateTextField.setMaxWidth(100);
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilDateTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(dateFormatter));
                HBox.setMargin(availableUntilDateTextField, new Insets(0, 15, 0, 25));
                rightHBox.getChildren().add(availableUntilDateTextField);
                validationSupport.addDateValidation(availableUntilDateTextField, "dd-MM-yyyy", availableUntilDateTextField, I18n.getI18nText("ValidationDateFormatIncorrect"));

                TextField availableUntilTimeTextField = new TextField();
                availableUntilTimeTextField.setPromptText("18:25");
                if (workingCurrentVideoScheduledItem.getExpirationDate() != null)
                    availableUntilTimeTextField.setText(workingCurrentVideoScheduledItem.getExpirationDate().format(timeFormatter));

                availableUntilTimeTextField.setMaxWidth(50);
                rightHBox.getChildren().add(availableUntilTimeTextField);
                validationSupport.addDateValidation(availableUntilTimeTextField, "HH:mm", availableUntilTimeTextField, I18n.getI18nText("ValidationTimeFormatIncorrect"));

                availableUntilDateTextField.textProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {

                        try {
                            LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                            LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                            // Combine the date and time to create LocalDateTime
                            LocalDateTime availableUntil = LocalDateTime.of(date, time);
                            workingCurrentVideoScheduledItem.setExpirationDate(availableUntil);
                        } catch (DateTimeParseException e) {
                        }
                    }
                });

                availableUntilTimeTextField.textProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                        try {
                            LocalDate date = LocalDate.parse(availableUntilDateTextField.getText(), dateFormatter);
                            LocalTime time = LocalTime.parse(availableUntilTimeTextField.getText(), timeFormatter);
                            // Combine the date and time to create LocalDateTime
                            LocalDateTime availableUntil = LocalDateTime.of(date, time);
                            workingCurrentVideoScheduledItem.setExpirationDate(availableUntil);
                        } catch (DateTimeParseException e) {
                        }
                    }
                });

                customContentAvailableUntilHBox.getChildren().add(rightHBox);
                customExpirationDateSwitch.selectedProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        if (!customExpirationDateSwitch.selectedProperty().get()) {
                            workingCurrentVideoScheduledItem.setExpirationDate(null);
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
                    if (!validationSupportInitialised[0]) {
                        FXProperties.runNowAndOnPropertiesChange(() -> {
                            if (I18n.getDictionary() != null) {
                                validationSupport.reset();
                            }
                        }, I18n.dictionaryProperty());
                        validationSupportInitialised[0] = true;
                    }

                    if (validationSupport.isValid()) {
                        localUpdateStore.submitChanges()
                            .onFailure(Console::log)
                            .onSuccess(x -> {
                                Console.log(x);
                                Platform.runLater(this::resetUpdateStoreAndOtherComponents);
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

        private HBox drawMediaLinkContainer(Media currentMedia, ObservableList<Media> mediaList, UpdateStore localUpdateStore) {
            HBox hBoxToReturn = new HBox();
            hBoxToReturn.setSpacing(20);

            TextField linkTextField = new TextField();
            linkTextField.setPromptText(I18n.getI18nText("EnterLinkHere"));
            linkTextField.setMinWidth(urlTextFieldWith);
            if (currentMedia != null) {
                linkTextField.setText(currentMedia.getUrl());
            }
            validationSupport.addUrlValidation(linkTextField, linkTextField, I18n.getI18nText("MalformedUrl"));

            // We update the value of the media according to the text field
            linkTextField.textProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    currentMedia.setUrl(linkTextField.getText());
                }
            });

            hBoxToReturn.getChildren().add(linkTextField);

            SVGPath removeButton = SvgIcons.createMinusPath();
            removeButton.getStyleClass().add(Bootstrap.TEXT_DANGER);
            MonoPane buttonContainer = new MonoPane(removeButton);
            buttonContainer.setCursor(Cursor.HAND);
            hBoxToReturn.setAlignment(Pos.CENTER_LEFT);
            hBoxToReturn.getChildren().add(buttonContainer);
            buttonContainer.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    localUpdateStore.deleteEntity(currentMedia);
                    mediaList.remove(currentMedia);
                }
            });

            return hBoxToReturn;
        }


    }
}
