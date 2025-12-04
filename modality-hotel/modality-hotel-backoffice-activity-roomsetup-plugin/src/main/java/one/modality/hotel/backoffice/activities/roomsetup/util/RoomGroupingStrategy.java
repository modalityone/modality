package one.modality.hotel.backoffice.activities.roomsetup.util;

import one.modality.base.shared.entities.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy interface for grouping rooms (ResourceConfiguration) by different criteria.
 * Implements the Strategy pattern to eliminate duplicate grouping logic across views.
 *
 * @author Claude Code
 */
public interface RoomGroupingStrategy {

    /**
     * Returns the group key for the given resource configuration.
     *
     * @param rc The resource configuration (room) to get the group key for
     * @param context Context containing lookup data (buildings, zones, pools, allocations)
     * @return The group key string
     */
    String getGroupKey(ResourceConfiguration rc, RoomGroupingContext context);

    /**
     * Groups a list of rooms by this strategy.
     *
     * @param rooms The rooms to group
     * @param context Context containing lookup data
     * @return Map of group key to list of rooms, in insertion order
     */
    default Map<String, List<ResourceConfiguration>> group(
            List<ResourceConfiguration> rooms,
            RoomGroupingContext context) {
        Map<String, List<ResourceConfiguration>> grouped = new LinkedHashMap<>();
        for (ResourceConfiguration rc : rooms) {
            String key = getGroupKey(rc, context);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(rc);
        }
        return grouped;
    }

    // ============== Concrete Strategy Implementations ==============

    /**
     * Groups rooms by their room type (Item).
     */
    class TypeGroupingStrategy implements RoomGroupingStrategy {
        @Override
        public String getGroupKey(ResourceConfiguration rc, RoomGroupingContext context) {
            Item item = rc.getItem();
            return item != null ? item.getName() : "Unknown Type";
        }
    }

    /**
     * Groups rooms by their building.
     */
    class BuildingGroupingStrategy implements RoomGroupingStrategy {
        @Override
        public String getGroupKey(ResourceConfiguration rc, RoomGroupingContext context) {
            Resource resource = rc.getResource();
            Building building = resource != null ? resource.getBuilding() : null;
            return building != null ? building.getName() : "Unassigned";
        }
    }

    /**
     * Groups rooms by their zone (with building prefix).
     */
    class ZoneGroupingStrategy implements RoomGroupingStrategy {
        @Override
        public String getGroupKey(ResourceConfiguration rc, RoomGroupingContext context) {
            Resource resource = rc.getResource();
            BuildingZone zone = resource != null ? resource.getBuildingZone() : null;
            if (zone != null) {
                Building building = zone.getBuilding();
                String prefix = building != null ? building.getName() + " — " : "";
                return prefix + zone.getName();
            }
            return "Unassigned";
        }
    }

    /**
     * Groups rooms by their pool allocation(s).
     */
    class PoolGroupingStrategy implements RoomGroupingStrategy {
        @Override
        public String getGroupKey(ResourceConfiguration rc, RoomGroupingContext context) {
            Resource resource = rc.getResource();
            if (resource == null) {
                return "⚠️ Unassigned";
            }

            List<PoolAllocation> allocations = context.getPoolAllocations();
            if (allocations == null || allocations.isEmpty()) {
                return "⚠️ Unassigned";
            }

            // Find all pool allocations for this resource (with null event = default allocation)
            List<String> poolNames = allocations.stream()
                    .filter(pa -> pa.getResource() != null
                            && pa.getResource().equals(resource)
                            && pa.getEvent() == null)
                    .map(pa -> pa.getPool() != null ? pa.getPool().getName() : "Unknown")
                    .distinct()
                    .collect(Collectors.toList());

            return poolNames.isEmpty() ? "⚠️ Unassigned" : String.join(", ", poolNames);
        }
    }
}
