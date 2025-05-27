package org.f1.parsing;

import org.f1.domain.PointEntityV2;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class CSVParsing {

    public static Set<PointEntityV2> parse(String fileName) {

        List<String> baseList = loadInput(fileName);

        String openingLine = baseList.removeFirst();
        List<String> openingParts = new ArrayList<>(List.of(openingLine.split(",")));



        Set<PointEntityV2> result = new HashSet<>();

        for(String line : baseList){
            List<String> parts = new ArrayList<>(List.of(line.split(",")));
            String name  = parts.removeFirst();
            Double cost = Double.parseDouble(parts.removeFirst());
            parts = parts.subList(0,parts.size()-6);
            List<Double> pointsList = parts.stream().map(Double::parseDouble).toList();


            result.add(new PointEntityV2(name,cost,pointsList));
        }

        return result;
    }

    private static List<String> loadInput(String fileName) {

        InputStream inputForDay = ClassLoader.getSystemResourceAsStream(fileName);
        if (Objects.isNull(inputForDay)) {
            throw new IllegalArgumentException("Can’t find data using filename: " + fileName + ". Did you forget to put the file in the resources directory?");
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(inputForDay))) {
            return r.lines().collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
