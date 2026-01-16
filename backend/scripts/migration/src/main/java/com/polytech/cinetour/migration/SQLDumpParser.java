package com.polytech.cinetour.migration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse un dump SQL MySQL et extrait les données
 */
public class SQLDumpParser {
    private static final Logger logger = LoggerFactory.getLogger(SQLDumpParser.class);

    private final String dumpFile;
    private final Map<String, List<List<Object>>> data;

    public SQLDumpParser(String dumpFile) {
        this.dumpFile = dumpFile;
        this.data = new HashMap<>();
    }

    public Map<String, List<List<Object>>> parse() throws IOException {
        logger.info("📖 Lecture du dump: {}", dumpFile);

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(dumpFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        String dumpContent = content.toString();

        // Extraire les données de chaque table
        data.put("movies", extractTableData(dumpContent, "movies"));
        data.put("persons", extractTableData(dumpContent, "persons"));
        data.put("directors", extractTableData(dumpContent, "directors"));
        data.put("director_directors", extractTableData(dumpContent, "director_directors"));
        data.put("locations", extractTableData(dumpContent, "locations"));
        data.put("users", extractTableData(dumpContent, "users"));
        data.put("ratings", extractTableData(dumpContent, "ratings"));
        data.put("principals", extractTableData(dumpContent, "principals"));
        data.put("person_primary_professions", extractTableData(dumpContent, "person_primary_professions"));
        data.put("person_known_for_titles", extractTableData(dumpContent, "person_known_for_titles"));
        data.put("movie_search_history", extractTableData(dumpContent, "movie_search_history"));

        logger.info("✓ Dump parsé avec succès");
        return data;
    }

    private List<List<Object>> extractTableData(String content, String tableName) {
        List<List<Object>> rows = new ArrayList<>();

        // Pattern pour trouver les INSERT INTO
        String patternStr = "INSERT INTO `" + tableName + "`.*?VALUES\\s+(.*?);";
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String valuesBlock = matcher.group(1);

            // Parser les tuples de valeurs
            Pattern tuplePattern = Pattern.compile("\\((.*?)\\)(?:,|\\s*$)", Pattern.DOTALL);
            Matcher tupleMatcher = tuplePattern.matcher(valuesBlock);

            while (tupleMatcher.find()) {
                String tupleContent = tupleMatcher.group(1);
                List<Object> values = parseValues(tupleContent);
                if (!values.isEmpty()) {
                    rows.add(values);
                }
            }
        }

        logger.info("  → {}: {} lignes", tableName, rows.size());
        return rows;
    }

    private List<Object> parseValues(String valueString) {
        List<Object> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < valueString.length(); i++) {
            char c = valueString.charAt(i);

            if (escapeNext) {
                currentValue.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            if (c == '\'' || c == '"') {
                if (!inString) {
                    inString = true;
                } else if (i > 0 && valueString.charAt(i - 1) != '\\') {
                    inString = false;
                } else {
                    currentValue.append(c);
                }
                continue;
            }

            if (c == ',' && !inString) {
                values.add(convertValue(currentValue.toString().trim()));
                currentValue = new StringBuilder();
                continue;
            }

            currentValue.append(c);
        }

        // Ajouter la dernière valeur
        String lastValue = currentValue.toString().trim();
        if (!lastValue.isEmpty()) {
            values.add(convertValue(lastValue));
        }

        return values;
    }

    private Object convertValue(String value) {
        value = value.trim();

        // NULL
        if (value.equalsIgnoreCase("NULL")) {
            return null;
        }

        // Boolean (bit) - vérifier AVANT les chaînes
        if (value.equals("b'0'")) {
            return false;
        }
        if (value.equals("b'1'")) {
            return true;
        }

        // Chaîne de caractères (retirer les quotes)
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1)
                    .replace("\\'", "'")
                    .replace("\\\"", "\"");
        }

        // Nombre
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public Map<String, List<List<Object>>> getData() {
        return data;
    }
}
