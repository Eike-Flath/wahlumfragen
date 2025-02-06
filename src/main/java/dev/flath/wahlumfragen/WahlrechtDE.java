package dev.flath.wahlumfragen;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class WahlrechtDE {

    public static final int TIMEOUT = 3000;

    private WahlrechtDE() {
        throw new RuntimeException();
    }

    private static List<Party> getParties(Element table) {
        Optional<Element> head = table.getElementsByTag("thead").stream().findAny();
        if (head.isEmpty())
            return Collections.emptyList();
        return head.get().getElementsByClass("part").stream()
                .map(e -> {
                    List<SingleParty> parties = e.getElementsByTag("a").stream().map(Element::text).map(SingleParty::new).toList();
                    if (parties.isEmpty())
                        return null;
                    else if (parties.size() == 1)
                        return parties.get(0);
                    else
                        return new PartyUnion(parties);
                })
                .toList();
    }

    private static Result resultFromRow(Institute institute, List<Party> parties, Element row) {
        final DateTimeFormatter fDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        final DateTimeFormatter fDayOfYear = DateTimeFormatter.ofPattern("dd.MM.");

        List<String> entries = row.getElementsByTag("td")
                .stream()
                .map(Element::text)
                .map(String::trim)
                .toList();
        List<Integer> permilles = entries.stream()
                .filter(s -> s.endsWith("%") || s.equals("–"))
                .map(s -> {
                    if (s.equals("–"))
                        return -1;
                    s = s.substring(0, s.length() - 1);
                    s = s.replace(',', '.');
                    try {
                        return Math.round(10 * Float.parseFloat(s));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .toList();
        if (permilles.size() < parties.size()) {
            System.err.printf("Failed to retrieve a result for institute '%s': not enough percentages\n", institute);
            return null;
        }
        for (Integer p : permilles) {
            if (p == null)
                return null;
        }
        Optional<LocalDate> date = entries.stream()
                .map(s -> {
                    try {
                        return LocalDate.parse(s, fDate);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findAny();
        if (date.isEmpty()) {
            System.err.printf("Failed to retrieve a result for institute '%s': no date\n", institute);
            return null;
        }
        LocalDate start = date.get(), end = date.get();
        Optional<String> range = entries.stream()
                .filter(s -> s.matches("\\d{2}.\\d{2}.–\\d{2}.\\d{2}."))
                .findAny();
        if (range.isPresent()) {
            // I am ashamed of this code
            int max_iter = 100;
            String[] parts = range.get().split("–");
            LocalDate startDOY = start;
            while (max_iter-- > 0 && !startDOY.format(fDayOfYear).equals(parts[0]))
                startDOY = startDOY.minusDays(1);
            LocalDate endDOY = end;
            while (max_iter-- > 0 && !endDOY.format(fDayOfYear).equals(parts[1]))
                endDOY = endDOY.minusDays(1);
            if (max_iter >= 0) {
                start = startDOY;
                end = endDOY;
            }
        }

        Map<Party, Integer> map = new HashMap<>();
        for (int i = 0; i < parties.size(); i++)
            map.put(parties.get(i), permilles.get(i));
        return new Result(institute, new DateRange(start, end), map);
    }

    public static Set<Result> retrieveResults(Institute institute) {
        URI instituteURI = institute.uri();
        Document doc;
        try {
            doc = Jsoup.parse(instituteURI.toURL(), TIMEOUT);
        } catch (IOException e) {
            System.err.printf("Failed to retrieve results for institute '%s': %s\n", institute, e);
            return Set.of();
        }
        Optional<Element> tableOpt = doc.getElementsByTag("table").stream()
                .filter(e -> e.hasClass("wilko"))
                .findAny();
        if (tableOpt.isEmpty()) {
            System.err.printf("Failed to retrieve results for institute '%s': unexpected format\n", institute);
            return Set.of();
        }
        Element table = tableOpt.get();
        Optional<Element> bodyOpt = table.getElementsByTag("tbody").stream().findAny();
        if (bodyOpt.isEmpty()) {
            System.err.printf("Failed to retrieve results for institute '%s': unexpected format\n", institute);
            return Set.of();
        }
        List<Party> parties = getParties(table);
        if (parties.isEmpty()) {
            System.err.printf("Failed to retrieve results for institute '%s': no parties\n", institute);
            return Set.of();
        }
        for (Party party : parties) {
            if (party == null) {
                System.err.printf("Failed to retrieve results for institute '%s': null party\n", institute);
                return Set.of();
            }
        }
        Set<Result> results = bodyOpt.get().getElementsByTag("tr").stream()
                .map(e -> resultFromRow(institute, parties, e))
                .collect(Collectors.toSet());
        results.remove(null);
        if (results.isEmpty()) {
            System.err.printf("Failed to retrieve results for institute '%s': no results\n", institute);
            return Set.of();
        }
        return results;
    }

}
