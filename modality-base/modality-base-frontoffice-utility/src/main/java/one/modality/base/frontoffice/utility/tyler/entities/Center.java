package one.modality.base.frontoffice.utility.tyler.entities;

import one.modality.base.shared.entities.Organization;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Center {
    public Double lat;
    public Double lng;
    public String name = "";
    public String type = "";
    public String city = "";
    public Organization organization;

    public static Set<String> rejected = new HashSet<>(Arrays.asList(
            "Kadampa", "Meditation", "Center",
            "Centre", "de", "Méditation",
            "Centro", "Meditación",
            "Bouddhiste",
            "Budista",
            "Zentrum", "für", "Buddhismus"
    ));

    public Center(String name, Double lat, Double lng, String type, String city) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.type = type;
        this.city = city;
    }

    public static void adjustRejectedList(List<Center> centers) {
        Map<String, Integer> counters = new HashMap<>();

        centers.forEach(c -> {
            for (String s : c.name.split(" ")) {
                counters.putIfAbsent(s, 0);
                counters.put(s, counters.get(s) + 1);
            }
        });

        counters.keySet().forEach(k -> {
            if (counters.get(k) > 10) {
                rejected.add(k);
            }
        });
    }

    public static String createFilterString(Center c) {
        List<String> toks = Arrays.asList(c.name.split(" ")).stream().filter(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !rejected.contains(s);
            }
        }).collect(Collectors.toList());

        return String.join(" ", toks);
    }

    public static boolean isGoodMatch(Center c1, Center c2) {
        String n1 = Center.createFilterString(c1);
        String n2 = Center.createFilterString(c2);
//        System.out.println(n1);
//        if (n1.equals("Knutsford Leisure Centre"))
//        System.out.println(n1 + " - " + n2);
//        System.out.println(n1 + " - " + n2);
        return n1.equals(n2) || c2.city.equals(c1.city);
    }
}