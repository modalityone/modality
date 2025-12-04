package one.modality.hotel.backoffice.activities.roomsetup.util;

import one.modality.base.shared.entities.Pool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for filtering pools by type (source vs category/event).
 * Eliminates duplicate pool filtering logic across views and dialogs.
 *
 * <p>In the KBS system, pools are categorized as:
 * <ul>
 *   <li><b>Source pools:</b> Where rooms "belong" by default (eventPool is null or false)</li>
 *   <li><b>Category/Event pools:</b> Used for event-time capacity planning (eventPool is true)</li>
 * </ul>
 *
 * @author Claude Code
 */
public final class PoolTypeFilter {

    private PoolTypeFilter() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if a pool is a source pool (default room allocation pool).
     *
     * @param pool The pool to check
     * @return true if the pool is a source pool (not an event pool)
     */
    public static boolean isSourcePool(Pool pool) {
        if (pool == null) {
            return false;
        }
        Boolean isEventPool = pool.isEventPool();
        return isEventPool == null || !isEventPool;
    }

    /**
     * Filters a list of pools to include only source pools.
     *
     * @param pools The list of pools to filter
     * @return A new list containing only source pools
     */
    public static List<Pool> filterSourcePools(List<Pool> pools) {
        if (pools == null) {
            return List.of();
        }
        return pools.stream()
                .filter(PoolTypeFilter::isSourcePool)
                .collect(Collectors.toList());
    }

}
