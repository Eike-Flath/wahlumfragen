package dev.flath.wahlumfragen;

import java.net.URI;
import java.util.*;

public record Institute(String id, String name) {

    private static final URI BASE_URI = URI.create("https://www.wahlrecht.de/umfragen/");

    public static List<Institute> INSTITUTES = List.of(
            new Institute("allensbach", "Allensbach"),
            new Institute("emnid", "Verian (Emnid)"),
            new Institute("forsa", "Forsa"),
            new Institute("politbarometer", "Forsch'gr. Wahlen"),
            new Institute("gms", "GMS"),
            new Institute("dimap", "Infratest dimap"),
            new Institute("insa", "INSA"),
            new Institute("yougov", "Yougov")
    );

    public static Map<String, Institute> INSTITUTES_BY_ID;

    static {
        Map<String, Institute> institutesById = new HashMap<>();
        for (Institute institute : INSTITUTES)
            institutesById.put(institute.id(), institute);
        INSTITUTES_BY_ID = Collections.unmodifiableMap(institutesById);
    }

    public URI uri() {
        return BASE_URI.resolve(id + ".htm");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Institute institute = (Institute) o;
        return Objects.equals(id, institute.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }

}
