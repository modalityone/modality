package one.modality.base.client.icons;

import dev.webfx.extras.imagestore.ImageStore;
import javafx.scene.image.ImageView;
import one.modality.base.shared.entities.Option;

/**
 * @author Bruno Salmon
 */
public final class ModalityIcons {

  public static final String addIcon16Url = "images/s16/actions/add.png";
  public static final String removeIcon16Url = "images/s16/actions/remove.png";
  public static final String attendanceIcon16Url = "images/s16/itemFamilies/attendance.png";
  public static final String checkedIcon16Url = "images/s16/checked.png";
  public static final String uncheckedIcon16Url = "images/s16/unchecked.png";
  public static final String spinnerIcon16Url = "images/s16/spinner.gif";

  public static final String certificateMonoSvgUrl = "images/svg/mono/certificate.svg";
  public static final String calendarMonoSvgUrl = "images/svg/mono/calendar.svg";
  public static final String priceTagMonoSvgUrl = "images/svg/mono/price-tag.svg";
  public static final String priceTagColorSvgUrl = "images/svg/color/price-tag.svg";

  public static final String addIcon16JsonUrl = getJsonUrl16(addIcon16Url);
  public static final String removeIcon16JsonUrl = getJsonUrl16(removeIcon16Url);
  public static final String attendanceIcon16JsonUrl = getJsonUrl16(attendanceIcon16Url);

  public static final String certificateMonoSvg16JsonUrl = getJsonUrl16(certificateMonoSvgUrl);
  public static final String calendarMonoSvg16JsonUrl = getJsonUrl16(calendarMonoSvgUrl);
  public static final String priceTagMonoSvg16JsonUrl = getJsonUrl16(priceTagMonoSvgUrl);
  public static final String priceTagColorSvg16JsonUrl = getJsonUrl16(priceTagColorSvgUrl);

  public static String getJsonUrl16(String url) {
    return "{url: '" + url + "', width: 16, height: 16}";
  }

  public static ImageView getLanguageIcon32(Object language) {
    return ImageStore.createImageView("images/s32/system/lang_" + language + ".png", 32, 32);
  }

  public static ImageView getItemFamilyIcon16(Option option) {
    return ImageStore.createImageView(
        "images/s16/itemFamilies/" + option.getItemFamilyCode() + ".png", 16, 16);
  }
}
