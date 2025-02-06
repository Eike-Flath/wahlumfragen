package dev.flath.wahlumfragen;

import java.util.*;
import java.util.stream.Collectors;

public final class PartyUnion implements Party {

    private final Set<SingleParty> parties;
    private final String id, name;

    public PartyUnion(Collection<SingleParty> parties) {
        this.parties = Set.copyOf(parties);
        name = this.parties.stream()
                .map(Party::name)
                .sorted()
                .collect(Collectors.joining("/"));
        id = name.toLowerCase(Locale.GERMAN);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartyUnion that = (PartyUnion) o;
        return Objects.equals(parties, that.parties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parties);
    }

}
