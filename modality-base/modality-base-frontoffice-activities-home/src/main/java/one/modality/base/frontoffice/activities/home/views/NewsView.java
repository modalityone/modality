package one.modality.base.frontoffice.activities.home.views;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.platform.util.Objects;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.activities.home.HomeActivity;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.News;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class NewsView {
    private News news;
    private final Label titleLabel = GeneralUtility.getMainLabel(null, StyleUtility.MAIN_BLUE);
    private final Text dateText = TextUtility.getSubText(null);
    private final Label excerptLabel = GeneralUtility.getMainLabel(null, StyleUtility.VICTOR_BATTLE_BLACK);
    private final ImageView imageView = new ImageView();
    private final Pane newsContainer = new Pane(imageView, titleLabel, dateText, excerptLabel) {
        private double imageY, imageWidth, rightX, rightWidth, dateY, dateHeight, titleY, titleHeight, excerptY, excerptHeight;
        @Override
        protected void layoutChildren() {
            computeLayout(getWidth());
            imageView.setFitWidth(imageWidth);
            layoutInArea(imageView, 0, imageY, imageWidth, 0, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(titleLabel, rightX, titleY, rightWidth, titleHeight, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(dateText, rightX, dateY, rightWidth, dateHeight, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(excerptLabel, rightX, excerptY, rightWidth, excerptHeight, 0, HPos.LEFT, VPos.TOP);
        }

        @Override
        protected double computeMinHeight(double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computeMaxHeight(double width) {
            return computePrefHeight(width);
        }

        @Override
        protected double computePrefHeight(double width) {
            computeLayout(width);
            return Math.max(imageY + imageView.prefHeight(imageWidth), excerptY + excerptHeight);
        }

        private void computeLayout(double width) {
            if (width == -1)
                width = getWidth();
            /* Image: */      imageY = 0;                          imageWidth = width / 3;
            /* Right side: */ rightX = imageWidth + 20;            rightWidth = width - rightX;
            /* Title: */      titleY = 0;                         titleHeight = titleLabel.prefHeight(rightWidth);
            /* Date: */        dateY = titleY + titleHeight + 5;   dateHeight = dateText.prefHeight(rightWidth);
            /* Excerpt: */  excerptY = dateY + dateHeight + 5;  excerptHeight = excerptLabel.prefHeight(rightWidth);
        }
    };

    private double screenPressedY;

    {
        imageView.setPreserveRatio(true);
        newsContainer.setCursor(Cursor.HAND);
        newsContainer.setOnMousePressed(e -> screenPressedY = e.getScreenY());
        newsContainer.setOnMouseReleased(e -> {
            if (Math.abs(e.getScreenY() - screenPressedY) < 10)
                browseNews();
        });
    }

    public void setNews(News news) {
        this.news = news;
        updateLabel(titleLabel, news.getTitle().toUpperCase());
        updateText(dateText, DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(news.getDate()));
        updateLabel(excerptLabel, news.getExcerpt());
        imageView.setImage(ImageStore.getOrCreateImage(news.getImageUrl()));

    }

    public Node getView() {
        return newsContainer;
    }

    private void browseNews() {
        HomeActivity.browse(news.getLinkUrl());
    }

    private static void updateText(Text text, String newContent) {
        if (!Objects.areEquals(newContent, text.getText()))
            text.setText(newContent);
    }

    private static void updateLabel(Label label, String newContent) {
        if (!Objects.areEquals(newContent, label.getText()))
            label.setText(newContent);
    }
}
