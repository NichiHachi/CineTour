package com.polytech.crud.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeocodingService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${geocoding.locationiq.apikey:}")
    private String locationIqApiKey;

    @Value("${selenium.remote.url}")
    private String seleniumRemoteUrl;

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&limit=5&q=";

    public GeocodingResult geocode(String address) {
        String expectedCountry = extractCountryFromAddress(address);
        
        // Essayer Nominatim via Selenium (simule un vrai navigateur)
        GeocodingResult result = geocodeWithNominatimViaBrowser(address, expectedCountry);
        if (result != null) {
            return result;
        }
        
        // Fallback: essayer plusieurs niveaux de simplification
        List<String> simplifiedAddresses = getSimplifiedAddresses(address);
        for (String simplified : simplifiedAddresses) {
            if (!simplified.equals(address)) {
                logger.info("Retrying with simplified address: {}", simplified);
                result = geocodeWithNominatimViaBrowser(simplified, expectedCountry);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Génère plusieurs versions simplifiées de l'adresse pour maximiser les chances de trouver un résultat.
     */
    private List<String> getSimplifiedAddresses(String address) {
        List<String> results = new ArrayList<>();
        if (address == null) return results;
        
        // Nettoyer l'adresse des préfixes
        String cleaned = address.replaceAll("^[0-9]+[A-Za-z]?\\s+", ""); // Enlever numéro de rue
        cleaned = cleaned.replaceAll("^[^,]+-\\s*", ""); // Enlever "Nom - " au début
        
        String[] parts = cleaned.split(",");
        
        // Niveau 1: 3 derniers éléments (si plus de 3)
        if (parts.length > 3) {
            results.add(joinParts(parts, parts.length - 3, parts.length));
        }
        
        // Niveau 2: 2 derniers éléments (ville/région + pays)
        if (parts.length >= 2) {
            results.add(joinParts(parts, parts.length - 2, parts.length));
        }
        
        // Niveau 3: dernier élément seulement (pays/territoire)
        if (parts.length >= 1) {
            results.add(parts[parts.length - 1].trim());
        }
        
        return results;
    }

    private String joinParts(String[] parts, int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(parts[i].trim());
        }
        return result.toString();
    }

    private GeocodingResult geocodeWithNominatimViaBrowser(String address, String expectedCountry) {
        WebDriver driver = null;
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = NOMINATIM_URL + encodedAddress;
            logger.info("Geocoding with Nominatim via browser: {}", address);

            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
            options.addArguments("--window-size=1920,1080");
            options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
            
            // Désactiver le JSON viewer de Firefox pour avoir le JSON brut
            options.addPreference("devtools.jsonview.enabled", false);

            driver = new RemoteWebDriver(new URI(seleniumRemoteUrl).toURL(), options);
            driver.get(url);

            // Attendre un peu que la page charge
            Thread.sleep(500);

            // Récupérer le JSON brut via JavaScript (plus fiable)
            String jsonContent = null;
            try {
                org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
                // Essayer de récupérer le texte du body directement
                jsonContent = (String) js.executeScript("return document.body.textContent || document.body.innerText;");
            } catch (Exception jsError) {
                logger.warn("JavaScript extraction failed: {}", jsError.getMessage());
            }
            
            if (jsonContent == null || jsonContent.isEmpty()) {
                // Fallback: récupérer la page source
                String pageSource = driver.getPageSource();
                jsonContent = extractJsonFromPage(pageSource);
            }
            
            if (jsonContent == null || jsonContent.isEmpty()) {
                logger.error("Could not extract JSON from page");
                return null;
            }

            // Nettoyer le contenu (enlever les espaces et retours à la ligne inutiles)
            jsonContent = jsonContent.trim();
            
            logger.info("Extracted JSON (first 300 chars): {}", 
                jsonContent.substring(0, Math.min(300, jsonContent.length())));
            
            return parseGeocodingResponse(jsonContent, address, expectedCountry);
        } catch (Exception e) {
            logger.error("Error geocoding with Nominatim via browser '{}': {}", address, e.getMessage());
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private String extractJsonFromPage(String pageSource) {
        if (pageSource == null || pageSource.isEmpty()) {
            return null;
        }
        
        String content = pageSource;
        
        // Essayer d'extraire le contenu du <pre> en premier (format JSON viewer de Firefox)
        if (pageSource.contains("<pre")) {
            int start = pageSource.indexOf("<pre");
            start = pageSource.indexOf(">", start) + 1;
            int end = pageSource.indexOf("</pre>", start);
            if (end > start) {
                content = pageSource.substring(start, end);
            }
        } else if (pageSource.contains("<body")) {
            int start = pageSource.indexOf("<body");
            start = pageSource.indexOf(">", start) + 1;
            int end = pageSource.indexOf("</body>", start);
            if (end > start) {
                content = pageSource.substring(start, end);
            }
        }
        
        // Supprimer toutes les balises HTML restantes
        content = content.replaceAll("<[^>]+>", "").trim();
        
        // Décoder les entités HTML
        content = content.replace("&quot;", "\"")
                         .replace("&amp;", "&")
                         .replace("&lt;", "<")
                         .replace("&gt;", ">")
                         .replace("&apos;", "'")
                         .replace("&#39;", "'")
                         .replace("&nbsp;", " ");
        
        // Chercher le début d'un tableau JSON
        int jsonStart = content.indexOf('[');
        if (jsonStart >= 0) {
            // Trouver la fin du tableau
            int depth = 0;
            int jsonEnd = -1;
            for (int i = jsonStart; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        jsonEnd = i + 1;
                        break;
                    }
                }
            }
            if (jsonEnd > jsonStart) {
                return content.substring(jsonStart, jsonEnd);
            }
        }
        
        return content.trim();
    }

    private String extractCountryFromAddress(String address) {
        if (address == null) return null;
        String[] parts = address.split(",");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1].trim().toLowerCase();
            if (lastPart.contains("usa") || lastPart.contains("united states") || lastPart.contains("u.s.")) return "us";
            if (lastPart.contains("uk") || lastPart.contains("united kingdom") || lastPart.contains("england") || lastPart.contains("scotland") || lastPart.contains("wales")) return "gb";
            if (lastPart.contains("france")) return "fr";
            if (lastPart.contains("canada")) return "ca";
            if (lastPart.contains("germany") || lastPart.contains("deutschland")) return "de";
            if (lastPart.contains("italy") || lastPart.contains("italia")) return "it";
            if (lastPart.contains("spain") || lastPart.contains("españa")) return "es";
            if (lastPart.contains("australia")) return "au";
            if (lastPart.contains("new zealand")) return "nz";
            if (lastPart.contains("japan")) return "jp";
            if (lastPart.contains("ireland")) return "ie";
            if (lastPart.contains("virgin islands") || lastPart.contains("u.s. virgin islands")) return "vi";
            
            if (parts.length >= 2) {
                String secondLast = parts[parts.length - 2].trim().toLowerCase();
                if (isUSState(secondLast)) return "us";
            }
        }
        return null;
    }

    private boolean isUSState(String text) {
        String[] usStates = {"alabama", "alaska", "arizona", "arkansas", "california", "colorado", 
            "connecticut", "delaware", "florida", "georgia", "hawaii", "idaho", "illinois", 
            "indiana", "iowa", "kansas", "kentucky", "louisiana", "maine", "maryland", 
            "massachusetts", "michigan", "minnesota", "mississippi", "missouri", "montana", 
            "nebraska", "nevada", "new hampshire", "new jersey", "new mexico", "new york", 
            "north carolina", "north dakota", "ohio", "oklahoma", "oregon", "pennsylvania", 
            "rhode island", "south carolina", "south dakota", "tennessee", "texas", "utah", 
            "vermont", "virginia", "washington", "west virginia", "wisconsin", "wyoming"};
        for (String state : usStates) {
            if (text.contains(state)) return true;
        }
        return false;
    }

    private GeocodingResult parseGeocodingResponse(String responseBody, String address, String expectedCountry) {
        try {
            JsonNode jsonArray = objectMapper.readTree(responseBody);
            logger.info("Parsed JSON array size: {}", jsonArray.size());

            if (!jsonArray.isArray() || jsonArray.size() == 0) {
                logger.warn("No results for: {}", address);
                return null;
            }

            // Parcourir les résultats pour trouver celui qui correspond au pays attendu
            for (JsonNode node : jsonArray) {
                String countryCode = null;
                if (node.has("address") && node.get("address").has("country_code")) {
                    countryCode = node.get("address").get("country_code").asText().toLowerCase();
                }

                if (expectedCountry != null && countryCode != null) {
                    boolean matches = countryCode.equals(expectedCountry) ||
                            (expectedCountry.equals("vi") && countryCode.equals("us")) ||
                            (expectedCountry.equals("us") && countryCode.equals("vi"));
                    
                    if (!matches) {
                        logger.debug("Skipping result with country {} (expected {})", countryCode, expectedCountry);
                        continue;
                    }
                }

                GeocodingResult result = new GeocodingResult();
                result.setLatitude(Double.parseDouble(node.get("lat").asText()));
                result.setLongitude(Double.parseDouble(node.get("lon").asText()));
                result.setDisplayName(node.get("display_name").asText());
                result.setCountryCode(countryCode != null ? countryCode.toUpperCase() : null);

                logger.info("Geocoded '{}' -> ({}, {}) [{}]", 
                    address, result.getLatitude(), result.getLongitude(), result.getCountryCode());
                return result;
            }

            // Si aucun résultat ne correspond au pays, prendre le premier
            logger.warn("No result matching expected country {} for '{}', using first result", expectedCountry, address);
            JsonNode firstResult = jsonArray.get(0);
            GeocodingResult result = new GeocodingResult();
            result.setLatitude(Double.parseDouble(firstResult.get("lat").asText()));
            result.setLongitude(Double.parseDouble(firstResult.get("lon").asText()));
            result.setDisplayName(firstResult.get("display_name").asText());
            if (firstResult.has("address") && firstResult.get("address").has("country_code")) {
                result.setCountryCode(firstResult.get("address").get("country_code").asText().toUpperCase());
            }
            return result;

        } catch (Exception e) {
            logger.error("Error parsing geocoding response for '{}': {}", address, e.getMessage());
            return null;
        }
    }

    public static class GeocodingResult {
        private Double latitude;
        private Double longitude;
        private String displayName;
        private String countryCode;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }
}