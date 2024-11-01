package one.modality.base.frontoffice.activities.account;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.frontoffice.utility.tyler.StyleUtility;
import one.modality.base.frontoffice.utility.tyler.TextUtility;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.client.i18n.EcommerceI18nKeys;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class BookingView {

    private static final String BOOKING_EVENT_REQUIRED_FIELDS = "event.(name,label,image.url,live,startDate,endDate,venue.(name,label,country),organization.country)";
    private static final String BOOKING_PERSON_REQUIRED_FIELDS = "ref,person_firstName,person_lastName";
    private static final String BOOKING_STATUS_REQUIRED_FIELDS = BookingStatus.BOOKING_REQUIRED_FIELDS;
    public  static final String BOOKING_REQUIRED_FIELDS = BOOKING_EVENT_REQUIRED_FIELDS + "," + BOOKING_PERSON_REQUIRED_FIELDS + "," + BOOKING_STATUS_REQUIRED_FIELDS;

    private final Document booking;

    private final Label eventNameLabel = GeneralUtility.createLabel(StyleUtility.MAIN_ORANGE_COLOR);
    private final Text eventCentreLocationText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    private final Text eventCountryLocationText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    private final Label personLabel = new Label();
    private final Label statusLabel = new Label();
    private final Text totalPriceLabel = I18n.newText(EcommerceI18nKeys.Total);
    private final Text totalPriceValue = new Text();
    private final Text remainingPriceLabel = I18n.newText(EcommerceI18nKeys.RemainingAmount);
    private final Text remainingPriceValue = new Text();
    private final Text paidPriceLabel = I18n.newText(EcommerceI18nKeys.Paid);
    private final Text paidPriceValue = new Text();
    private final List<Button> buttons = new ArrayList<>();

    private final Pane containerPane = new Pane(
        // Row1:
        eventNameLabel, personLabel, statusLabel,
        // Row2:
        eventCentreLocationText, eventCountryLocationText,
        // Row3:
        totalPriceLabel, totalPriceValue, remainingPriceLabel, remainingPriceValue, paidPriceLabel, paidPriceValue
        // Row4:
        // => buttons will be added later depending on the booking status
        ) {
        double lastComputeLayoutWidth;
        double left, row1Y, row1Height, eventNameWidth, eventNameHeight, personLabelX, personLabelWidth, statusLabelHeight;
        double row2Y, row2Height;
        double row3Y, row3Height;
        double row4Y, row4Height, button1X, buttonsHGap = 10; // buttons
        double bottomY;

        {
            setMaxWidth(Double.MAX_VALUE);
            setMinHeight(0);
            setMaxHeight(Double.MAX_VALUE);
            setPadding(new Insets(10));
            setBackground(Background.fill(Color.web("#f4f4f4")));
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected void layoutChildren() {
            double width = getWidth();
            computeLayout(width);
            Insets insets = getInsets();
            width -= insets.getLeft() + insets.getRight();

            // Row1:
            layoutInArea(eventNameLabel, left, row1Y, eventNameWidth, eventNameHeight, 0, null, HPos.LEFT, VPos.TOP);
            double statusLabelWidth = statusLabel.prefWidth(row1Height);
            layoutInArea(personLabel, personLabelX, row1Y, personLabelWidth, statusLabelHeight, 0, null, HPos.CENTER, VPos.CENTER);
            layoutInArea(statusLabel, width - statusLabelWidth, row1Y, statusLabelWidth, statusLabelHeight, 0, null, HPos.LEFT, VPos.TOP);

            // Row2:
            double eventCentreLocationWidth = eventCentreLocationText.prefWidth(row2Height);
            layoutInArea(eventCentreLocationText, left, row2Y, eventCentreLocationWidth, row2Height, 0, null, HPos.LEFT, VPos.TOP);
            layoutInArea(eventCountryLocationText, left + eventCentreLocationWidth + 10, row2Y, width - eventCentreLocationWidth, row2Height, 0, null, HPos.LEFT, VPos.TOP);

            // Row3:
            double labelWidth = totalPriceLabel.prefWidth(row3Height);
            double valueWidth = totalPriceValue.prefWidth(row3Height);
            double labelValueWidth = labelWidth + 10 + valueWidth;
            double labelX = width * 0.2 - labelValueWidth / 2;
            double valueX = labelX + labelWidth + 10;
            layoutInArea(totalPriceLabel, labelX, row3Y, labelWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);
            layoutInArea(totalPriceValue, valueX, row3Y, valueWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);

            labelWidth = remainingPriceLabel.prefWidth(row3Height);
            valueWidth = remainingPriceValue.prefWidth(row3Height);
            labelValueWidth = labelWidth + 10 + valueWidth;
            labelX = width * 0.5 - labelValueWidth / 2;
            valueX = labelX + labelWidth + 10;
            layoutInArea(remainingPriceLabel, labelX, row3Y, labelWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);
            layoutInArea(remainingPriceValue, valueX, row3Y, valueWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);

            labelWidth = paidPriceLabel.prefWidth(row3Height);
            valueWidth = paidPriceValue.prefWidth(row3Height);
            labelValueWidth = labelWidth + 10 + valueWidth;
            labelX = width * 0.8 - labelValueWidth / 2;
            valueX = labelX + labelWidth + 10;
            layoutInArea(paidPriceLabel, labelX, row3Y, labelWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);
            layoutInArea(paidPriceValue, valueX, row3Y, valueWidth, row3Height, 0, null, HPos.LEFT, VPos.TOP);

            // Row4:
            double buttonX = button1X;
            for (Button button : buttons) {
                double buttonWidth = button.prefWidth(row4Height);
                layoutInArea(button, buttonX, row4Y, buttonWidth, row4Y, 0, null, HPos.LEFT, VPos.TOP);
                buttonX += buttonsHGap + buttonWidth;
            }
        }

        @Override
        protected double computePrefHeight(double width) {
            computeLayout(width);
            return bottomY;
        }

        private void computeLayout(double width) {
            if (width == -1)
                width = getWidth();
            if (lastComputeLayoutWidth == width)
                return;
            lastComputeLayoutWidth = width;
            Insets insets = getInsets();
            left = insets.getLeft();
            row1Y = insets.getTop();
            width -= left + insets.getRight();
            double fontFactor = GeneralUtility.computeFontFactor(width);
            GeneralUtility.setLabeledFont(eventNameLabel, StyleUtility.TEXT_FAMILY, FontWeight.SEMI_BOLD, fontFactor * 11);
            GeneralUtility.setLabeledFont(statusLabel,    StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 11);
            GeneralUtility.setLabeledFont(personLabel,    StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 9);
            Font smallFont = personLabel.getFont();
            eventCentreLocationText.setFont(smallFont);
            eventCountryLocationText.setFont(smallFont);
            totalPriceLabel.setFont(smallFont);
            totalPriceValue.setFont(smallFont);
            remainingPriceLabel.setFont(smallFont);
            remainingPriceValue.setFont(smallFont);
            paidPriceLabel.setFont(smallFont);
            paidPriceValue.setFont(smallFont);

            eventNameWidth = width / 3; row1Height = eventNameHeight = eventNameLabel.prefHeight(eventNameWidth);
            personLabelWidth = personLabel.prefWidth(row1Height); personLabelX = width / 2 - personLabelWidth / 2;
            statusLabelHeight = statusLabel.prefHeight(width);
            row2Y = row1Y + row1Height; row2Height = Math.max(eventCentreLocationText.prefHeight(width), eventCountryLocationText.prefHeight(width));
            row3Y = row2Y + row2Height + 10; row3Height = totalPriceValue.prefHeight(width);
            row4Y = row3Y + row3Height + 10; row4Height = buttons.isEmpty() ? 0 : buttons.get(0).prefHeight(-1);
            double buttonsWidth = buttons.stream().mapToDouble(b -> b.prefWidth(row4Height)).sum();
            button1X = width / 2 - buttonsWidth / 2 - buttonsHGap * (buttons.size() - 1);
            bottomY = row4Y + row4Height + insets.getBottom();
        }
    };

    public BookingView(Document booking) {
        this.booking = booking;
        Event event = booking.getEvent();
        setEvent(event);

        BookingStatus bookingStatus = BookingStatus.ofBooking(booking);
        I18nControls.bindI18nProperties(statusLabel, bookingStatus.getI18nKey());

        personLabel.setText(booking.getFullName());
        personLabel.setTextFill(Color.GRAY);

        totalPriceValue.setText(EventPriceFormatter.formatWithCurrency(booking.getPriceNet(), event));
        totalPriceValue.setFill(Color.GRAY);
        remainingPriceValue.setText(EventPriceFormatter.formatWithCurrency(booking.getPriceNet() - booking.getPriceDeposit(), event));
        remainingPriceValue.setFill(Color.GRAY);
        paidPriceValue.setText(EventPriceFormatter.formatWithCurrency(booking.getPriceDeposit(), event));
        paidPriceValue.setFill(Color.GRAY);

        if (bookingStatus == BookingStatus.PAYMENT_REQUIRED) {
            buttons.add(createPaymentButton());
        }

        containerPane.getChildren().addAll(buttons);
    }

    public void setEvent(Event event) {
        I18nControls.bindI18nProperties(eventNameLabel, new I18nSubKey("expression: i18n(this)", event));
        I18n.bindI18nProperties(eventCentreLocationText, new I18nSubKey("expression: '[At] ' + coalesce(i18n(venue), i18n(organization))", event));
        I18n.bindI18nProperties(eventCountryLocationText, new I18nSubKey("expression: coalesce(i18n(venue.country), i18n(organization.country))", event));
    }

    private Button createPaymentButton() {
        return Bootstrap.dangerButton(I18nControls.newButton(EcommerceI18nKeys.Pay, EventPriceFormatter.formatWithCurrency(booking.getPriceNet() - booking.getPriceDeposit(), booking.getEvent())));
    }

    public Node getView() {
        return containerPane;
    }

    public Document getBooking() {
        return booking;
    }

}
