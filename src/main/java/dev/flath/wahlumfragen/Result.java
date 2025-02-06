package dev.flath.wahlumfragen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Result(Institute institute, DateRange date, Map<Party, Integer> permille) {

    public Result(Institute institute, DateRange date, Map<Party, Integer> permille) {
        this.institute = institute;
        this.date = date;
        this.permille = new HashMap<>(permille);
    }

    public void normalize(Set<Party> parties) {
        for (Party party : parties)
            permille.putIfAbsent(party, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return Objects.equals(date, result.date) && Objects.equals(institute, result.institute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(institute, date);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('\'').append(institute).append("' (");
        result.append(date).append("): ");
        result.append(permille.entrySet().stream()
                .sorted((e1, e2) -> -Integer.compare(e1.getValue(), e2.getValue()))
                .filter(e -> e.getValue() >= 0)
                .map(e -> String.format("%s: %.1f", e.getKey(), 0.1f * e.getValue()))
                .collect(Collectors.joining(", ")));
        return result.toString();
    }
}
