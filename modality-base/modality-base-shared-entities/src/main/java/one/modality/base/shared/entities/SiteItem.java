package one.modality.base.shared.entities;

import one.modality.base.shared.entities.markers.HasItem;
import one.modality.base.shared.entities.markers.HasSite;

import java.util.Objects;

/**
 * Convenient class that holds a site and item combination.
 *
 * @author Bruno Salmon
 */
public final class SiteItem {

    private final Site site;
    private final Item item;

    public <T extends HasSite & HasItem> SiteItem(T t) {
        this(t.getSite(), t.getItem());
    }

    public SiteItem(Site site, Item item) {
        this.site = site;
        this.item = item;
    }

    public Site getSite() {
        return site;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        SiteItem siteItem = (SiteItem) o;
        return Objects.equals(site, siteItem.site) && Objects.equals(item, siteItem.item);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(site);
        result = 31 * result + Objects.hashCode(item);
        return result;
    }
}
