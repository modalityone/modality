package one.modality.hotel.backoffice.activities.roomsetup.util;

import one.modality.base.shared.entities.*;

import java.util.List;

/**
 * Context object providing lookup data for room grouping strategies.
 * Contains references to buildings, zones, pools, and allocations
 * that may be needed by different grouping strategies.
 *
 * @author Claude Code
 */
public class RoomGroupingContext {

    private final List<Building> buildings;
    private final List<Pool> pools;
    private final List<PoolAllocation> poolAllocations;

    /**
     * Creates a context with all lookup data.
     */
    public RoomGroupingContext(
            List<Building> buildings,
            List<Pool> pools,
            List<PoolAllocation> poolAllocations) {
        this.buildings = buildings;
        this.pools = pools;
        this.poolAllocations = poolAllocations;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public List<PoolAllocation> getPoolAllocations() {
        return poolAllocations;
    }
}
