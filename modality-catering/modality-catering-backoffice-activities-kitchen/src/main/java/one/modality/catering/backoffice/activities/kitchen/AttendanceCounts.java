package one.modality.catering.backoffice.activities.kitchen;

import one.modality.base.shared.entities.Attendance;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceCounts {

    @Deprecated
    List<LocalDate> dates;
    private Map<LocalDate, Map<String, Integer>> countsForDate = new HashMap<>();

    public void add(LocalDate date, String item) {
        if (!countsForDate.containsKey(date)) {
            countsForDate.put(date, new HashMap<>());
        }
        Map<String, Integer> map = countsForDate.get(date);
        int previousCount = map.getOrDefault(item, Integer.valueOf(0));
        map.put(item, Integer.valueOf(previousCount + 1));
    }

    public void remove(LocalDate date, String item) {
        if (countsForDate.containsKey(date)) {
            Map<String, Integer> map = countsForDate.get(date);
            int previousCount = map.getOrDefault(item, Integer.valueOf(1));
            map.put(item, Integer.valueOf(previousCount - 1));
        }
    }

    public List<LocalDate> getSortedDates() {
        return countsForDate.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getSortedKeys() {
        return countsForDate.values().stream()
                .map(entry -> entry.keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int getCount(LocalDate date, String item) {
        if (!countsForDate.containsKey(date)) {
            return 0;
        }
        Map<String, Integer> map = countsForDate.get(date);
        return map.getOrDefault(item, Integer.valueOf(0));
    }
}
