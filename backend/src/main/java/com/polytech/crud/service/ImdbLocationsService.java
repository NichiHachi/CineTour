package com.polytech.crud.service;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.entity.Location;
import com.polytech.crud.entity.Movie;
import com.polytech.crud.repository.LocationRepository;
import com.polytech.crud.repository.MovieRepository;
import com.polytech.utils.Console;

@Service
public class ImdbLocationsService {
    private static final Logger logger = LoggerFactory.getLogger(ImdbLocationsService.class);

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ImdbMoviesService imdbMoviesService;

    @Autowired
    private GeocodingService geocodingService;

    @Value("${selenium.remote.url}")
    private String seleniumRemoteUrl;

    private final String imdbLocationsUrl = "https://www.imdb.com/title/%s/locations";

    /**
     * Crée un WebDriver Firefox configuré pour le scraping.
     */
    private WebDriver createWebDriver() throws Exception {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
        options.addArguments("--window-size=1920,1080");
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        return new RemoteWebDriver(new URI(seleniumRemoteUrl).toURL(), options);
    }

    /**
     * Scrape filming locations from IMDb for a given movie ID.
     */
    private List<Location> scrapeLocations(String movieIdImdb) throws IOException {
        List<Location> locations = new ArrayList<>();
        String url = String.format(imdbLocationsUrl, movieIdImdb);
        WebDriver driver = null;

        try {
            driver = createWebDriver();
            driver.get(url);

            // Wait for content to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

            // Check if there are no locations
            try {
                WebElement noLocationsMessage = driver.findElement(
                        By.xpath("//p[contains(text(), \"It looks like we don't have any filming & production\")]"));
                if (noLocationsMessage != null) {
                    logger.info("No locations available for movie {}", movieIdImdb);
                    return locations;
                }
            } catch (NoSuchElementException e) {
                logger.debug("No 'no locations' message found for movie {}. Continuing with location scraping.",
                        movieIdImdb);
            }

            // Try to find and click "Show more" button if it exists
            try {
                WebElement showMoreButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(
                                "span.ipc-see-more.sc-33e570c-0.cMGrFN.single-page-see-more-button-flmg_locations button")));

                // Scroll to the button to ensure it is visible
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", showMoreButton);
                Thread.sleep(500); // Wait for scrolling to complete

                // Retry mechanism for clicking the button
                int retryCount = 0;
                boolean clicked = false;
                while (retryCount < 3 && !clicked) {
                    try {
                        showMoreButton.click();
                        clicked = true;
                        logger.info("Clicked 'Show more' button for movie {}", movieIdImdb);
                    } catch (ElementClickInterceptedException e) {
                        retryCount++;
                        Thread.sleep(500); // Wait before retrying
                    }
                }
                if (!clicked) {
                    logger.warn("Failed to click 'Show more' button after {} attempts for movie {}", retryCount,
                            movieIdImdb);
                } else {
                    Thread.sleep(1000); // Wait for content to load after click
                }
            } catch (TimeoutException e) {
                logger.info("No 'Show more' button found for movie {}", movieIdImdb);
            }

            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource);

            // Updated selectors for current IMDb structure
            Elements locationElements = doc.select("div[data-testid='item-id']");
            logger.info("Found {} location elements", locationElements.size() - 1);

            locationElements.forEach(el -> {
                String locationString = el.select("a[data-testid='item-text-with-link']").text().trim();
                String description = el.select("p[data-testid='item-attributes']").text().trim();

                if (!locationString.isEmpty()) {
                    Location location = new Location();
                    location.setIdImdb(movieIdImdb);
                    location.setLocationString(locationString);
                    location.setDescription(description);
                    location.setGeocodingFailed(false);
                    locations.add(location);
                    logger.info("Found location: {} with description: {}", locationString, description);
                }
            });

        } catch (Exception e) {
            logger.error("Failed to scrape locations for movie {}: {}", movieIdImdb, e.getMessage(), e);
            throw new IOException("Failed to scrape locations", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }

        return locations;
    }

    /**
     * Imports filming locations for a movie from IMDb and saves them to the database.
     */
    @Transactional
    public void importLocations(String movieIdImdb) throws Exception {
        Movie movie = movieRepository.findByIdImdb(movieIdImdb);
        if (movie == null) {
            logger.info("Movie {} not found in database", movieIdImdb);
            return;
        }

        if (Boolean.TRUE.equals(movie.getLocationsChecked())) {
            logger.info("Movie {} location(s) (if they exist) has been already searched, skipping import", movieIdImdb);
            return;
        }

        try {
            List<Location> locations = scrapeLocations(movieIdImdb);
            movie.setLocationsChecked(true);
            movieRepository.save(movie);

            if (locations.isEmpty()) {
                logger.info("No locations found for movie {}", movieIdImdb);
                return;
            }
            locationRepository.saveAll(locations);
            logger.info("Successfully imported {} locations for movie {}", locations.size(), movieIdImdb);
        } catch (Exception e) {
            logger.error("Failed to import locations for movie {}: {}", movieIdImdb, e.getMessage());
            throw e;
        }
    }

    /**
     * Geocode a single location and update its coordinates.
     *
     * @return true if location was modified, false otherwise
     */
    public boolean geocodeLocation(Location location) {
        if (location == null || location.getLocationString() == null) {
            logger.warn("Cannot geocode null location or location without address");
            return false;
        }

        // Skip si déjà géocodé
        if (location.getLatitude() != null && location.getLongitude() != null) {
            logger.debug("Location already geocoded: {}", location.getLocationString());
            return false;
        }

        // Skip si géocodage déjà échoué
        if (Boolean.TRUE.equals(location.getGeocodingFailed())) {
            logger.debug("Location geocoding previously failed, skipping: {}", location.getLocationString());
            return false;
        }

        logger.info("Geocoding location: {}", location.getLocationString());
        GeocodingService.GeocodingResult result = geocodingService.geocode(location.getLocationString());

        if (result == null) {
            logger.warn("Geocoding failed for location: {}", location.getLocationString());
            location.setGeocodingFailed(true);
            return true; // Modifié pour sauvegarder le flag
        }

        location.setLatitude(result.getLatitude());
        location.setLongitude(result.getLongitude());
        location.setDisplayName(result.getDisplayName());
        if (result.getCountryCode() != null) {
            location.setCountryCode(result.getCountryCode());
        }
        location.setGeocodingFailed(false);

        logger.info("Geocoded: {} -> ({}, {}) [{}]",
            location.getLocationString(),
            result.getLatitude(),
            result.getLongitude(),
            result.getCountryCode());

        return true;
    }

    /**
     * Geocode locations that need it, with rate limiting.
     *
     * @return number of locations modified
     */
    private int geocodeLocationsWithRateLimit(List<Location> locations) {
        int updatedCount = 0;
        for (Location location : locations) {
            if (location.getLatitude() == null && !Boolean.TRUE.equals(location.getGeocodingFailed())) {
                if (geocodeLocation(location)) {
                    updatedCount++;
                }
                try {
                    Thread.sleep(1100); // Respecter les limites de l'API Nominatim (1 req/sec)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Geocoding sleep interrupted");
                    break;
                }
            }
        }

        if (updatedCount > 0) {
            locationRepository.saveAll(locations);
            logger.info("Updated {} location(s)", updatedCount);
        }
        return updatedCount;
    }

    /**
     * Migrate all existing locations by geocoding them.
     * Only updates locations missing coordinates and not previously failed.
     */
    public void migrateExistingLocations() throws InterruptedException {
        List<Location> locations = locationRepository.findAll();
        logger.info("Found {} locations to check", locations.size());
        int geocoded = 0;
        int skipped = 0;
        int failed = 0;

        for (Location location : locations) {
            // Skip si déjà géocodé
            if (location.getLatitude() != null && location.getLongitude() != null) {
                skipped++;
                continue;
            }
            // Skip si déjà échoué
            if (Boolean.TRUE.equals(location.getGeocodingFailed())) {
                skipped++;
                continue;
            }

            if (geocodeLocation(location)) {
                locationRepository.save(location);
                if (location.getLatitude() != null) {
                    geocoded++;
                    logger.info("Geocoded {}: {} -> ({}, {})", geocoded, location.getLocationString(),
                            location.getLatitude(), location.getLongitude());
                } else {
                    failed++;
                }
            }

            Thread.sleep(1100); // Respecter les limites de l'API (1 req/sec)
        }

        logger.info("Migration completed: {} geocoded, {} failed, {} skipped", geocoded, failed, skipped);
    }

    /**
     * Get locations by IMDb ID, geocoding on demand if needed.
     * Increments the location search count for the movie.
     */
    @Transactional
    public List<Location> getLocationsByImdbId(String movieIdImdb) {
        Movie movie = movieRepository.findByIdImdb(movieIdImdb);
        if (movie == null) {
            logger.info("No movie found for IMDB ID: {}", movieIdImdb);
            return Collections.emptyList();
        }

        // Incrémenter le compteur directement (évite self-invocation)
        movie.setLocationSearchCount(movie.getLocationSearchCount() + 1);
        movieRepository.save(movie);
        logger.debug("Incremented location search count for movie {} to {}", movieIdImdb, movie.getLocationSearchCount());

        List<Location> locations = locationRepository.findByIdImdb(movieIdImdb);

        // Géocoder les locations qui n'ont pas encore de coordonnées
        geocodeLocationsWithRateLimit(locations);

        return locations;
    }

    /**
     * Get locations by movie database ID.
     */
    @Transactional(readOnly = true)
    public List<Location> getLocationsById(Long movieId) {
        Movie movie = movieRepository.findById(movieId.intValue())
                .orElse(null);

        if (movie == null) {
            logger.info("No movie found for ID: {}", movieId);
            return Collections.emptyList();
        }

        return locationRepository.findByIdImdb(movie.getIdImdb());
    }

    /**
     * Get locations by movie title.
     * Imports locations from IMDB if not already checked.
     * Increments the location search count for each movie.
     */
    @Transactional
    public List<Location> getLocationsByTitle(String title) {
        List<Movie> movies = movieRepository.findByTitle(title);
        List<Location> allLocations = new ArrayList<>();

        for (Movie movie : movies) {
            movie.setLocationSearchCount(movie.getLocationSearchCount() + 1);
            movieRepository.save(movie);
            logger.debug("Incremented location search count for movie {} to {}", movie.getIdImdb(),
                    movie.getLocationSearchCount());

            List<Location> locations = locationRepository.findByIdImdb(movie.getIdImdb());
            if (locations.isEmpty() && !Boolean.TRUE.equals(movie.getLocationsChecked())) {
                try {
                    importLocations(movie.getIdImdb());
                    locations = locationRepository.findByIdImdb(movie.getIdImdb());
                } catch (Exception e) {
                    logger.error("Error importing locations for movie {}: {}", movie.getIdImdb(), e.getMessage());
                }
            }
            allLocations.addAll(locations);
        }
        return allLocations;
    }

    /**
     * Get all locations from the database.
     */
    @Transactional(readOnly = true)
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}

