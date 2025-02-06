package dev.flath.wahlumfragen;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record DateRange(LocalDate start, LocalDate end) implements Comparable<DateRange> {

    @Override
    public String toString() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format("%s - %s", start.format(formatter), end.format(formatter));
    }

    @Override
    public int compareTo(DateRange o) {
        return end.compareTo(o.end);
    }

}
