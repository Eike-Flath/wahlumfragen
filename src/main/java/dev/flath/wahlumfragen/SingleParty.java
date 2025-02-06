package dev.flath.wahlumfragen;

import java.util.Locale;
import java.util.Objects;

public record SingleParty(String id, String name) implements Party {

    public SingleParty(String name) {
        this(name.toLowerCase(Locale.GERMAN), name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SingleParty that = (SingleParty) o;
        return Objects.equals(id, that.id);
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
