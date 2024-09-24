package one.modality.event.backoffice.activities.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.switches.Switch;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.validation.ModalityValidationSupport;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.MediaType;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.event.fx.FXEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MediaLinksManagement {
    BorderPane mainContainer = new BorderPane();
    Item linkedItem;
    protected EntityStore entityStore;
    protected ObservableList<LocalDate> teachingsDates;
    protected ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase;
    protected ObservableList<ScheduledItem> scheduledItemsLinkedToTeachingScheduledItemsReadFromDatabase;
    protected ObservableList<Media> recordingsMediasReadFromDatabase;

    public MediaLinksManagement(Item linkedItem, EntityStore entityStore,ObservableList<LocalDate> teachingsDates,ObservableList<ScheduledItem> teachingsScheduledItemsReadFromDatabase,ObservableList<Media> recordingsMediasReadFromDatabase) {
        this.linkedItem = linkedItem;
        this.entityStore = entityStore;
        this.recordingsMediasReadFromDatabase = recordingsMediasReadFromDatabase;
        this.teachingsDates = teachingsDates;
        this.teachingsScheduledItemsReadFromDatabase = teachingsScheduledItemsReadFromDatabase;
    }

    protected BorderPane computeTeachingDateLine(LocalDate date) {
        MediaLinksPerDateManagement mediaLinksPerDateManagement = new MediaLinksPerDateManagement(date);
        return mediaLinksPerDateManagement.drawPanel();
    }

    private void reinitialiseRecordingsMediasReadFromDatabase() {
        entityStore.executeQuery(
                new EntityStoreQuery("select url, scheduledItem.parent, scheduledItem.item, scheduledItem.date, scheduledItem.item.family.code, published from Media where scheduledItem.event= ? and scheduledItem.item.family.code = '"+linkedItem.getFamily().getCode()+"'", new Object[] { FXEvent.getEvent()}))
            .onFailure(Console::log)
            .onSuccess(query -> Platform.runLater(() -> {
                EntityList<Media> mediasList = query.getStore().getEntityList(query.getListId());
                recordingsMediasReadFromDatabase.setAll(mediasList);
                }));
    }

    public void updatePercentageProperty(LocalDate date,IntegerProperty percentageProperty,StringProperty cssProperty) {
        long numberOfTeachingForThisDayAndLanguage = teachingsScheduledItemsReadFromDatabase.stream()
            .filter(item -> item.getDate().equals(date))
            .count();

        // Calculate the number of media for this day and language
        long numberOfScheduledItemLinkedToMediaForThisDay = recordingsMediasReadFromDatabase.stream()
            .filter(media -> media.getScheduledItem().getDate().equals(date) &&
                media.getScheduledItem().getItem().equals(linkedItem))
            .map(Media::getScheduledItem)
            .filter(item -> item.getDate().equals(date))  // Récupère le ScheduledItem associé à chaque Media
            .distinct()                    // Supprime les doublons si nécessaire (facultatif)
            .count();

        // Calculate percentage, handling division by zero
        percentageProperty.set((int) (numberOfTeachingForThisDayAndLanguage == 0 ? 0 :
            (100.0 * numberOfScheduledItemLinkedToMediaForThisDay / numberOfTeachingForThisDayAndLanguage)));

        if (percentageProperty.get() < 100) {
            cssProperty.setValue(Bootstrap.TEXT_DANGER );
        } else {
            cssProperty.setValue(Bootstrap.TEXT_SUCCESS);
        }
    }
    public ObservableList<Media> getRecordingsMediasReadFromDatabase() {
        return recordingsMediasReadFromDatabase;
    }
    public void setVisible(boolean visible) {
        mainContainer.setVisible(visible);
        mainContainer.setManaged(visible);
    }
    public BorderPane getContainer() {
        return mainContainer;
    }

    protected class MediaLinksPerDateManagement {
        protected final LocalDate currentDate;
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);
        ModalityValidationSupport validationSupport = new ModalityValidationSupport();
        boolean[] validationSupportInitialised = {false};
        DateTimeFormatter dateFormatterToDisplayCurrentDay = DateTimeFormatter.ofPattern("d MMMM, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        IntegerProperty percentageProperty = new SimpleIntegerProperty();
        StringProperty cssProperty = new SimpleStringProperty();
        List<ScheduledItem> filteredListForCurrentDay;
        ObservableList<Media> workingMedias = FXCollections.observableArrayList();


        protected MediaLinksPerDateManagement(LocalDate date) {
            currentDate = date;

        }

        protected BorderPane drawPanel() {
            /* The content with the list of the teachings per day and the links **/
            filteredListForCurrentDay = teachingsScheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate))
                .collect(Collectors.toList());
            workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().equals(linkedItem)).
                map(updateStore::updateEntity).collect(Collectors.toList()));
            BorderPane container = new BorderPane();

            container.setBackground(Background.fill(Color.WHITE));
            //We make an array which only one boolean instead of a boolean, so we can use it in lambda expression
            final boolean[] isCenterVisible = {false};

            VBox centerVBox = new VBox();
            container.setCenter(centerVBox);
            centerVBox.setVisible(false);
            centerVBox.setManaged(false);
            //We create a separate method for building the header line because it will likely be the same for the child classes, but the content below will change
            HBox headerLine = buildHeaderLine(centerVBox,isCenterVisible);
            container.setTop(headerLine);
            Separator separator = new Separator();
            centerVBox.getChildren().add(separator);

            /* The content with the list of the teachings per day and the links **/
            List<ScheduledItem> filteredListForCurrentDay = teachingsScheduledItemsReadFromDatabase.stream()
                .filter(item -> item.getDate().equals(currentDate))
                .collect(Collectors.toList());

            for (ScheduledItem teachingScheduledItem : filteredListForCurrentDay) {

                /* Here we create the line for each teaching **/
                HBox currentLine = new HBox();
                currentLine.setPadding(new Insets(20, 20, 20, 40));

                //TODO: when the timeline will have a name, we will take the name from the timeline
                String name = teachingScheduledItem.getName();
                if (name == null) name = "Unknown";
                Label teachingTitle = new Label(name);
                Label startTimeLabel = new Label(teachingScheduledItem.getTimeline().getStartTime().format(timeFormatter) + " - " + teachingScheduledItem.getTimeline().getEndTime().format(timeFormatter));
                teachingTitle.getStyleClass().add(Bootstrap.STRONG);
                startTimeLabel.getStyleClass().add(Bootstrap.STRONG);
                VBox teachingDetailsVBox = new VBox(teachingTitle, startTimeLabel);
                teachingDetailsVBox.setAlignment(Pos.CENTER);

                Label publishedLabel = new Label("Published");
                Switch publishedSwitch = new Switch();
                HBox publishedInfo = new HBox(publishedLabel,publishedSwitch);
                publishedInfo.setSpacing(5);
                publishedInfo.setVisible(false);

                TextField linkTextField = new TextField();
                linkTextField.setPromptText(I18n.getI18nText("Link"));
                linkTextField.setPrefWidth(500);
                //  validationSupport.addUrlOrEmptyValidation(linkTextField, linkTextField, "UrlIsMalformed");
                validationSupport.addUrlOrEmptyValidation(linkTextField, linkTextField, I18n.getI18nText("MalformedUrl"));

                //We look if there is an existing media for this teaching
                List<Media> mediaList = workingMedias.stream()
                    .filter(media -> media.getScheduledItem().getParent().equals(teachingScheduledItem))
                    .collect(Collectors.toList());

                //We create an ArrayList of Media that will contain only one element instead of a Media to be able to use it in lambda expression
                final ArrayList<Media> currentMedia = new ArrayList<>();

                if (!mediaList.isEmpty()) {
                    currentMedia.add(mediaList.get(0));
                    publishedInfo.setVisible(true);
                    publishedSwitch.setSelected(currentMedia.get(0).isPublished());
                    linkTextField.setText(currentMedia.get(0).getUrl());

                }

                linkTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                    //If there is a change and the mediaList for this teaching is empty, we create the Recording Scheduled Item and the Media associated
                    if (mediaList.isEmpty()) {
                        ScheduledItem currentRecordingScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                        currentRecordingScheduledItem.setParent(teachingScheduledItem);
                        currentRecordingScheduledItem.setSite(teachingScheduledItem.getSite());
                        currentRecordingScheduledItem.setEvent(teachingScheduledItem.getEvent());
                        currentRecordingScheduledItem.setItem(linkedItem);
                        currentRecordingScheduledItem.setDate(teachingScheduledItem.getDate());

                        Media createdMedia = updateStore.insertEntity(Media.class);
                        createdMedia.setScheduledItem(currentRecordingScheduledItem);
                        createdMedia.setType(MediaType.AUDIO);
                        currentMedia.add(createdMedia);
                        mediaList.add(createdMedia);
                    }
                    currentMedia.get(0).setUrl(newValue);
                    publishedInfo.setVisible(true);

                    //If the new value is empty, we delete the recording scheduledItem and the media associated
                    if (newValue.isEmpty() && !mediaList.isEmpty()) {
                        updateStore.deleteEntity(currentMedia.get(0));
                        updateStore.deleteEntity(currentMedia.get(0).getScheduledItem());
                        mediaList.remove(currentMedia.get(0));
                        currentMedia.clear();
                        publishedInfo.setVisible(false);

                    }
                });

                //currentMedia is necessarily not empty here.
                publishedSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> currentMedia.get(0).setPublished(newValue));
                Region anotherSpacer = new Region();
                HBox.setHgrow(anotherSpacer, Priority.ALWAYS);

                currentLine.setSpacing(10);
                currentLine.getChildren().setAll(teachingDetailsVBox, anotherSpacer, publishedInfo, linkTextField);

                centerVBox.getChildren().add(currentLine);
            }

            HBox lastLineHox = buildLastLine();
            centerVBox.getChildren().add(lastLineHox);

            container.setBorder(new Border(new BorderStroke(
                Color.BLACK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(3),
                new BorderWidths(1)
            )));
            return container;
        }

        protected HBox buildLastLine() {
            Button saveButton = Bootstrap.largeSuccessButton(I18nControls.bindI18nProperties(new Button(), "Save"));
            saveButton.disableProperty().bind(updateStore.hasChangesProperty().not());
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
                    updateStore.submitChanges()
                        .onFailure(Console::log)
                        .onSuccess(x -> Platform.runLater(this::resetUpdateStoreAndOtherComponents));
                }
            });
            HBox hBoxToReturn = new HBox(saveButton);
            hBoxToReturn.setAlignment(Pos.CENTER_RIGHT);
            hBoxToReturn.setPadding(new Insets(0, 40, 20, 0));
            return hBoxToReturn;
        }
        protected HBox buildHeaderLine(VBox centerVBox,boolean[] isCenterVisible) {
            HBox hBoxToReturn = new HBox();
            hBoxToReturn.setAlignment(Pos.CENTER_LEFT);
            hBoxToReturn.setPadding(new Insets(10, 20, 10, 20));

            Label dateLabel = new Label(currentDate.format(dateFormatterToDisplayCurrentDay));
            TextTheme.createPrimaryTextFacet(dateLabel).style();
            hBoxToReturn.getChildren().add(dateLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hBoxToReturn.getChildren().add(spacer);

            //We add a listener to dynamically update the percentage text and css property
            recordingsMediasReadFromDatabase.addListener((InvalidationListener) observable -> {
                updatePercentageProperty(currentDate,percentageProperty,cssProperty);
                workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().equals(linkedItem)).
                    map(updateStore::updateEntity).collect(Collectors.toList()));

            });

            Label percentageLabel = new Label();
            percentageLabel.textProperty().bind(Bindings.format("%d%%", percentageProperty));
            percentageLabel.setPadding(new Insets(0, 30, 0, 0));
            hBoxToReturn.getChildren().add(percentageLabel);

            cssProperty.addListener((obs, oldClass, newClass) -> {
                percentageLabel.getStyleClass().removeAll(oldClass); // Remove the old class
                percentageLabel.getStyleClass().add(newClass);       // Add the new class
            });
            updatePercentageProperty(currentDate,percentageProperty,cssProperty);

            SVGPath topArrowButton = SvgIcons.createTopArrowPath();
            SVGPath bottomArrowButton = SvgIcons.createBottomArrowPath();
            topArrowButton.setFill(Color.web("#0096D6"));
            bottomArrowButton.setFill(Color.web("#0096D6"));
            MonoPane buttonContainer = new MonoPane(bottomArrowButton);

            buttonContainer.setOnMouseClicked(e -> {
                if (isCenterVisible[0]) {
                    // Hide the center pane with animation
                    centerVBox.setVisible(false);
                    centerVBox.setManaged(false);
                    isCenterVisible[0] = false;
                    buttonContainer.getChildren().setAll(bottomArrowButton);
                } else {
                    centerVBox.setVisible(true);
                    centerVBox.setManaged(true);
                    isCenterVisible[0] = true;
                    buttonContainer.getChildren().setAll(topArrowButton);
                }
            });
            buttonContainer.setCursor(Cursor.HAND);

            hBoxToReturn.getChildren().add(buttonContainer);
            hBoxToReturn.setBackground(Background.fill(Color.LIGHTGRAY));
            return hBoxToReturn;
        }

        public void resetUpdateStoreAndOtherComponents() {
            reinitialiseRecordingsMediasReadFromDatabase();
            workingMedias.setAll(recordingsMediasReadFromDatabase.stream().filter(media -> media.getScheduledItem().getDate().equals(currentDate) && media.getScheduledItem().getItem().equals(linkedItem)).
                map(updateStore::updateEntity).collect(Collectors.toList()));
        }
    }


}
