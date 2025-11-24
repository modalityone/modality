package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.i18n.BaseI18nKeys;

/**
 * @author Bruno Salmon
 */
public class StandardBookingFormNavigation implements BookingFormNavigation {

    private final GridPane gridPane = new GridPane();
    private final ColumnConstraints c1 = new ColumnConstraints();
    private final AriaToggleGroup<Object> barToggleGroup = new AriaToggleGroup<>();
    private final ToggleButton backButton = createNavigationButton(BaseI18nKeys.Back);
    private final ToggleButton nextButton = createNavigationButton(BaseI18nKeys.Next);
    private final Label titleLabel = Bootstrap.strong(Bootstrap.textPrimary(new Label()));
    private final ObjectProperty<Object> titleI18nKeyProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            I18nControls.bindI18nProperties(titleLabel, get());
        }
    };
    private MultiPageBookingForm bookingForm;

    public StandardBookingFormNavigation() {
        backButton.setGraphic(createChevron(true));
        nextButton.setGraphic(createChevron(false));
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Controls.setupTextWrapping(titleLabel, true, false);
        titleLabel.setPadding(new Insets(0, 5, 0, 5));
        GridPane.setHgrow(titleLabel, Priority.ALWAYS);
        c1.setHalignment(HPos.CENTER);
        FXProperties.runOnPropertiesChange(o -> {
            updateButtons();
        }, gridPane.widthProperty(), nextButton.disabledProperty());
        ColumnConstraints c2 = new ColumnConstraints();
        gridPane.getColumnConstraints().addAll(c1, c2, c1);
        RowConstraints r1 = new RowConstraints();
        r1.setMinHeight(51);
        gridPane.getRowConstraints().add(r1);
        gridPane.add(backButton, 0, 0);
        gridPane.add(titleLabel, 1, 0);
        gridPane.add(nextButton, 2, 0);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getStyleClass().add("navigation-bar");
        titleLabel.getStyleClass().add("title");
        backButton.getStyleClass().add("back-button");
        nextButton.getStyleClass().add("next-button");
        backButton.visibleProperty().bind(backButton.disabledProperty().not());
        nextButton.visibleProperty().bind(nextButton.disabledProperty().not());
    }

    @Override
    public Node getView() {
        return gridPane;
    }

    @Override
    public ToggleButton getBackButton() {
        return backButton;
    }

    @Override
    public ToggleButton getNextButton() {
        return nextButton;
    }

    @Override
    public void setBookingForm(MultiPageBookingForm bookingForm) {
        this.bookingForm = bookingForm;
    }

    @Override
    public void updateState() {
        if (bookingForm != null && bookingForm.displayedPage != null) {
            titleI18nKeyProperty.set(bookingForm.displayedPage.getTitleI18nKey());
            boolean lastPage = bookingForm.getDisplayedPageIndex() == bookingForm.getPages().length - 1;
            I18nControls.bindI18nProperties(nextButton, lastPage ? BaseI18nKeys.Submit : BaseI18nKeys.Next);
        }
        updateButtons();
    }

    private void updateButtons() {
        Color gray = Color.web("#E7E7E7");
        Color blue = Color.web("#0096D6");
        BorderWidths borderWidths = new BorderWidths(2);
        CornerRadii radii = new CornerRadii(8);
        double w = gridPane.getWidth();
        boolean mobileView = w < 500;
        backButton.setContentDisplay(mobileView ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY);
        nextButton.setContentDisplay(mobileView ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY);
        double buttonWidth = mobileView ? 60 : Math.max(100, 0.2 * w);
        c1.setMinWidth(buttonWidth);
        Paint paint = nextButton.isDisabled() ? gray
                : new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, gray),
                        new Stop((w - 1.2 * buttonWidth) / w, gray),
                        new Stop((w - buttonWidth) / w, blue),
                        new Stop(1, blue));
        gridPane.setBackground(new Background(new BackgroundFill(paint, radii, Insets.EMPTY)));
        gridPane.setBorder(new Border(new BorderStroke(paint, BorderStrokeStyle.SOLID, radii, borderWidths)));
    }

    private ToggleButton createNavigationButton(Object i18nKey) {
        ToggleButton button = Bootstrap
                .textSecondary(I18nControls.bindI18nProperties(barToggleGroup.createItemButton(i18nKey), i18nKey));
        button.setAlignment(Pos.CENTER);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setCursor(Cursor.HAND);
        return button;
    }

    private SVGPath createChevron(boolean back) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(back ? "M21 10 l-8 8 l8 8" : "M15 10 l8 8 l-8 8");
        svgPath.setFill(null);
        svgPath.setStroke(Color.WHITE);
        svgPath.setStrokeWidth(3); // Scaled down from 8 (also by 2/3)
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        return svgPath;
    }
}
