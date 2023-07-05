package one.modality.base.frontoffice.states;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class GeneralPM {
    public static StringProperty ACCOUNT_NAME = new SimpleStringProperty("Paul Meetbol");
    public static ImageView ACCOUNT_IMG = new ImageView("/one/modality/base/frontoffice/avatar-generations.jpeg");
    public static ObjectProperty<Node> LANGUAGE_BUTTON_OPTIONS = new SimpleObjectProperty<>(new VBox());

    public static String HOME_PATH = "/app-home";
    public static String HOME_NEWS_ARTICLE_PATH = "/app-home-news-article";
    public static String ALERTS_PATH = "/app-alerts";
    public static String BOOKING_PATH = "/app-booking";
    public static String ACCOUNT_PATH = "/app-account";
    public static String ACCOUNT_PERSONAL_INFORMATION_PATH = "/app-account/personal-information";
    public static String ACCOUNT_SETTINGS_PATH = "/app-account/settings";
    public static String ACCOUNT_FRIENDS_AND_FAMILY_PATH = "/app-account/friends-family";
    public static String ACCOUNT_FRIENDS_AND_FAMILY_EDIT_PATH = "/app-account/friends-family/edit";

}
