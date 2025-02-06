package dev.flath.wahlumfragen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        Set<Result> results = new HashSet<>();
        for (Institute institute : Institute.INSTITUTES) {
            Set<Result> fromInstitute = WahlrechtDE.retrieveResults(institute);
            System.err.printf("Retrieved %,d results from '%s'%n", fromInstitute.size(), institute.uri());
            results.addAll(fromInstitute);
        }
        Set<Party> parties = new HashSet<>();
        for (Result result : results)
            parties.addAll(result.permille().keySet());
        System.err.printf("Found %d parties: %s%n", parties.size(), parties);
        for (Result result : results)
            result.normalize(parties);
        List<Result> resultList = results.stream()
                .sorted(Comparator.comparing(Result::date).reversed())
                .toList();
        System.err.printf("Retrieved %,d results in total%n", results.size());
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<>() {
                    @Override
                    public void write(JsonWriter jsonWriter, Object o) throws IOException {
                        if (o instanceof LocalDate d) {
                            jsonWriter.value(d.format(DateTimeFormatter.ISO_LOCAL_DATE));
                        }
                    }

                    @Override
                    public Object read(JsonReader jsonReader) {
                        return null;
                    }
                })
                .registerTypeAdapter(Institute.class, new TypeAdapter<>() {
                    @Override
                    public void write(JsonWriter jsonWriter, Object o) throws IOException {
                        if (o instanceof Institute i) {
                            jsonWriter.value(i.name());
                        }
                    }

                    @Override
                    public Object read(JsonReader jsonReader) {
                        return null;
                    }
                })
                .create();
        Writer writer = Files.newBufferedWriter(
                Path.of("results.json"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
        );
        gson.toJson(resultList, writer);
        writer.close();
        System.err.printf("Written result to 'results.json'%n");
    }

}
