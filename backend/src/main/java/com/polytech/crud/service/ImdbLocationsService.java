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
import org.openqa.selenium.chrome.ChromeOptions;
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

    @Value("${selenium.remote.url}")
    private String seleniumRemoteUrl;

    private final String imdbLocationsUrl = "https://www.imdb.com/title/%s/locations";

    /**
     * Scrape filming locations from IMDb for a given movie ID (IMDb ID).
     * Uses Selenium WebDriver to scrape dynamic content.
     * 
     * @param movieIdImdb String IMDb ID of the movie
     * @return List of Location objects
     * @throws IOException
     */
    private List<Location> scrapeLocations(String movieIdImdb) throws IOException {
        List<Location> locations = new ArrayList<>();
        String url = String.format(imdbLocationsUrl, movieIdImdb);
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
            options.addArguments("--window-size=1920,1080");
            options.addArguments(
                    "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");

            driver = new RemoteWebDriver(new URI(seleniumRemoteUrl).toURL(), options);
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
     * Imports filming locations for a movie from IMDb and saves them to the
     * database.
     * 
     * This method performs the following steps:
     * 1. Validates if the movie exists in the database
     * 2. Checks if locations have already been searched for this movie (using
     * locationsChecked flag)
     * 3. If not previously checked:
     * - Scrapes locations from IMDb using Selenium
     * - Marks the movie as checked in database
     * - If locations are found, saves them to database
     * 
     * The method is idempotent - multiple calls with the same movieIdImdb will only
     * scrape locations once. Subsequent calls will be skipped if the movie is
     * marked
     * as checked or if locations already exist.
     * 
     * @param movieIdImdb The IMDb ID of the movie (e.g., "tt0068646" for The
     *                    Godfather)
     * @throws Exception If an error occurs during web scraping or database
     *                   operations
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
            movie.setLocationsChecked(true); // Mark as checked regardless of result
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
     * Import filming locations for a list of movies from IMDb and save them to the
     * database.
     * Checks if locations already exist in the database before importing (to avoid
     * having to import them again).
     * 
     * @param movies List of Movie
     * @throws Exception
     */
    @Transactional
    public void importLocationsOfMovies(List<Movie> movies) {
        for (Movie movie : movies) {
            try {
                if (locationExistsByIdImdb(movie.getIdImdb())) {
                    Console.warnln("No locations found for movie " + movie.getIdImdb() + "in database\n");
                    importLocations(movie.getIdImdb());
                } else {
                    Console.warnln("Locations already imported for movie " + movie.getIdImdb() + "\n");
                    continue;
                }
            } catch (Exception e) {
                Console.errorln(
                        "Failed to import locations for movie " + movie.getIdImdb() + ": " + e.getMessage() + "\n");
            }
        }
        Console.warnln("Locations imported for all movies\n");
    }

    @Transactional(readOnly = true)
    public boolean locationExistsByIdImdb(String movieIdImdb) {
        return !locationRepository.findByIdImdb(movieIdImdb).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean locationExistsByTitle(String title) {
        return !getLocationsByTitle(title).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<Location> getLocationsByImdbId(String movieIdImdb) {
        return locationRepository.findByIdImdb(movieIdImdb);
    }

    /**
     * Retrieves a list of locations by their ID.
     * 
     * Uses `Optional` to safely and explicitly handle the absence of a result.
     * 
     * @param movieId The unique identifier of the location in the database.
     * @return A list containing a list of locations if it exists, an empty list
     *         otherwise.
     */
    @Transactional(readOnly = true)
    public List<Location> getLocationsById(Long movieId) {
        Movie movie = movieRepository.findById(movieId.intValue())
                .orElse(null);

        if (movie == null) {
            logger.info("No movie found for ID: {}", movieId);
            return Collections.emptyList();
        }

        List<Location> locations = locationRepository.findByIdImdb(movie.getIdImdb());
        return locations;
    }

    /**
     * Retrieves a list of locations by their title.
     * 
     * This method searches for movies by their title, then retrieves the filming
     * locations for each movie.
     * If no locations are found for a movie in database, it attempts to import the
     * locations from IMDb.
     * 
     * @param title The title of the location in the database.
     * @return A list containing a list of locations if it exists, an empty list
     *         otherwise.
     */
    @Transactional
    public List<Location> getLocationsByTitle(String title) {
        List<Movie> movies = movieRepository.findByTitle(title);
        List<Location> allLocations = new ArrayList<>();

        for (Movie movie : movies) {
            incrementLocationCount(movie);
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

    @Transactional
    private void incrementLocationCount(Movie movie) {
        movie.setLocationSearchCount(movie.getLocationSearchCount() + 1);
        movieRepository.save(movie);
        logger.debug("Incremented search locations count for movie {} to {}", movie.getIdImdb(),
                movie.getLocationSearchCount());
    }

    @Transactional(readOnly = true)
    public List<Movie> getMovieByTitle(String title) {
        return movieRepository.findByTitle(title);
    }

    @Transactional(readOnly = true)
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}