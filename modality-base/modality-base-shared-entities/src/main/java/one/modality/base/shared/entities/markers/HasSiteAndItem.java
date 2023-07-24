package one.modality.base.shared.entities.markers;

/**
 * @author Bruno Salmon
 */
public interface HasSiteAndItem extends HasSite, HasItem {

  default boolean hasSiteAndItem() {
    return hasSite() && hasItem();
  }
}
