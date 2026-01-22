package com.polytech.crud.neo4j.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.polytech.crud.neo4j.entity.LocationNode;
import com.polytech.crud.neo4j.repository.LocationNodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!import")
@RequiredArgsConstructor
@Transactional
public class LocationGraphService {

    private final LocationNodeRepository locationNodeRepository;

    private static final int GRID_CELL_SIZE_KM = 50;
    private static final int MAX_BATCH_SIZE = 1000;
    private static final int PARALLEL_THRESHOLD = 100;

    private static class GridCell {
        final int x;
        final int y;

        GridCell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof GridCell))
                return false;
            GridCell gridCell = (GridCell) o;
            return x == gridCell.x && y == gridCell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private static class IndexedLocation {
        final LocationNode location;
        final int index;
        final GridCell cell;

        IndexedLocation(LocationNode location, int index, GridCell cell) {
            this.location = location;
            this.index = index;
            this.cell = cell;
        }
    }

    private static class RNGEdge {
        final Long fromId;
        final Long toId;
        final double distance;

        RNGEdge(Long fromId, Long toId, double distance) {
            this.fromId = fromId;
            this.toId = toId;
            this.distance = distance;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private GridCell getGridCell(double lat, double lon) {
        int x = (int) Math.floor(lon * 111.0 / GRID_CELL_SIZE_KM);
        int y = (int) Math.floor(lat * 111.0 / GRID_CELL_SIZE_KM);
        return new GridCell(x, y);
    }

    private List<GridCell> getNeighboringCells(GridCell cell) {
        List<GridCell> neighbors = new ArrayList<>(9);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                neighbors.add(new GridCell(cell.x + dx, cell.y + dy));
            }
        }
        return neighbors;
    }

    private Map<GridCell, List<IndexedLocation>> buildSpatialIndex(List<LocationNode> locations) {
        Map<GridCell, List<IndexedLocation>> spatialIndex = new HashMap<>();

        for (int i = 0; i < locations.size(); i++) {
            LocationNode loc = locations.get(i);
            GridCell cell = getGridCell(loc.getLatitude(), loc.getLongitude());
            IndexedLocation indexed = new IndexedLocation(loc, i, cell);
            spatialIndex.computeIfAbsent(cell, k -> new ArrayList<>()).add(indexed);
        }

        return spatialIndex;
    }

    private double[][] buildDistanceMatrix(List<LocationNode> locations) {
        int n = locations.size();
        double[][] distMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            LocationNode loc1 = locations.get(i);
            distMatrix[i][i] = 0.0;

            for (int j = i + 1; j < n; j++) {
                LocationNode loc2 = locations.get(j);
                double dist = calculateDistance(
                        loc1.getLatitude(), loc1.getLongitude(),
                        loc2.getLatitude(), loc2.getLongitude());
                distMatrix[i][j] = dist;
                distMatrix[j][i] = dist;
            }
        }

        return distMatrix;
    }

    private List<IndexedLocation> getCandidateNeighbors(
            IndexedLocation loc,
            Map<GridCell, List<IndexedLocation>> spatialIndex) {

        List<IndexedLocation> candidates = new ArrayList<>();
        List<GridCell> neighborCells = getNeighboringCells(loc.cell);

        for (GridCell cell : neighborCells) {
            List<IndexedLocation> cellLocations = spatialIndex.get(cell);
            if (cellLocations != null) {
                candidates.addAll(cellLocations);
            }
        }

        return candidates;
    }

    private boolean areRelativeNeighbors(
            int idx1,
            int idx2,
            double[][] distMatrix,
            List<IndexedLocation> candidatePoints) {

        double distPQ = distMatrix[idx1][idx2];

        for (IndexedLocation indexed : candidatePoints) {
            int idx3 = indexed.index;

            if (idx3 == idx1 || idx3 == idx2) {
                continue;
            }

            double distPR = distMatrix[idx1][idx3];
            double distQR = distMatrix[idx2][idx3];

            if (Math.max(distPR, distQR) < distPQ) {
                return false;
            }
        }

        return true;
    }

    private List<RNGEdge> processBatch(
            List<IndexedLocation> locations,
            int startIdx,
            int endIdx,
            double[][] distMatrix,
            Map<GridCell, List<IndexedLocation>> spatialIndex) {

        List<RNGEdge> edges = new ArrayList<>();

        for (int i = startIdx; i < endIdx; i++) {
            IndexedLocation loc1 = locations.get(i);
            List<IndexedLocation> candidates = getCandidateNeighbors(loc1, spatialIndex);

            for (IndexedLocation loc2 : candidates) {
                int j = loc2.index;

                if (j <= i)
                    continue;

                if (areRelativeNeighbors(i, j, distMatrix, locations)) {
                    double distance = distMatrix[i][j] * 1000.0;
                    edges.add(new RNGEdge(
                            loc1.location.getId(),
                            loc2.location.getId(),
                            distance));
                }
            }
        }

        return edges;
    }

    @Transactional
    private void batchCreateRelationships(List<RNGEdge> edges) {
        if (edges.isEmpty())
            return;

        for (int i = 0; i < edges.size(); i += MAX_BATCH_SIZE) {
            int endIdx = Math.min(i + MAX_BATCH_SIZE, edges.size());
            List<RNGEdge> batch = edges.subList(i, endIdx);

            for (RNGEdge edge : batch) {
                locationNodeRepository.createNearRelationship(
                        edge.fromId,
                        edge.toId,
                        edge.distance);
            }
        }
    }

    @Transactional
    public int buildRelativeNeighborhoodGraph() {
        List<LocationNode> locations = locationNodeRepository.findAll().stream()
                .filter(loc -> loc.getLatitude() != null && loc.getLongitude() != null)
                .filter(loc -> !Boolean.TRUE.equals(loc.getGeocodingFailed()))
                .collect(Collectors.toList());

        int n = locations.size();

        if (n < 2) {
            return 0;
        }

        locationNodeRepository.deleteAllNearRelationships();

        Map<GridCell, List<IndexedLocation>> spatialIndex = buildSpatialIndex(locations);
        List<IndexedLocation> indexedLocations = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            LocationNode loc = locations.get(i);
            GridCell cell = getGridCell(loc.getLatitude(), loc.getLongitude());
            indexedLocations.add(new IndexedLocation(loc, i, cell));
        }

        double[][] distMatrix = buildDistanceMatrix(locations);

        List<RNGEdge> allEdges;

        if (n >= PARALLEL_THRESHOLD) {
            allEdges = buildRNGParallel(indexedLocations, distMatrix, spatialIndex);
        } else {
            allEdges = processBatch(indexedLocations, 0, n, distMatrix, spatialIndex);
        }

        batchCreateRelationships(allEdges);

        return allEdges.size();
    }

    private List<RNGEdge> buildRNGParallel(
            List<IndexedLocation> locations,
            double[][] distMatrix,
            Map<GridCell, List<IndexedLocation>> spatialIndex) {

        int n = locations.size();
        int numThreads = Runtime.getRuntime().availableProcessors();
        int chunkSize = Math.max(10, n / (numThreads * 2));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<List<RNGEdge>>> futures = new ArrayList<>();

        for (int start = 0; start < n; start += chunkSize) {
            final int startIdx = start;
            final int endIdx = Math.min(start + chunkSize, n);

            Future<List<RNGEdge>> future = executor
                    .submit(() -> processBatch(locations, startIdx, endIdx, distMatrix, spatialIndex));
            futures.add(future);
        }

        List<RNGEdge> allEdges = new ArrayList<>();
        for (Future<List<RNGEdge>> future : futures) {
            try {
                allEdges.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return allEdges;
    }

    public List<LocationWithDistance> getNeighbors(Long locationId) {
        return locationNodeRepository.findNeighbors(locationId).stream()
                .map(projection -> new LocationWithDistance(
                        projection.getNeighbor(),
                        projection.getDistance()))
                .collect(Collectors.toList());
    }

    public List<LocationNode> findShortestPath(Long fromLocationId, Long toLocationId) {
        return locationNodeRepository.findShortestPath(fromLocationId, toLocationId);
    }

    public List<String> getNearbyMovieImdbIds(List<Long> locationIds, String excludeImdbId, int minMovies) {
        Set<String> foundMovies = new HashSet<>();
        int depth = 1;
        int maxDepth = 10;

        while (foundMovies.size() < minMovies && depth <= maxDepth) {
            List<String> locationIdStrings = locationIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            List<String> movies = locationNodeRepository.findNearbyMoviesByBFS(
                    locationIdStrings, excludeImdbId, depth);
            foundMovies.addAll(movies);
            depth++;
        }

        return new ArrayList<>(foundMovies);
    }

    @Transactional
    public int addLocationToRNG(Long locationId) {
        LocationNode newLocation = locationNodeRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        if (newLocation.getLatitude() == null || newLocation.getLongitude() == null
                || Boolean.TRUE.equals(newLocation.getGeocodingFailed())) {
            return 0;
        }

        double searchRadius = GRID_CELL_SIZE_KM * 1.5;
        List<LocationNode> candidates = locationNodeRepository.findNearby(
                newLocation.getLatitude(),
                newLocation.getLongitude(),
                searchRadius);

        List<RNGEdge> edges = new ArrayList<>();

        for (LocationNode candidate : candidates) {
            if (candidate.getId().equals(locationId))
                continue;

            double distPQ = calculateDistance(
                    newLocation.getLatitude(), newLocation.getLongitude(),
                    candidate.getLatitude(), candidate.getLongitude());

            boolean isRNGNeighbor = true;

            for (LocationNode other : candidates) {
                if (other.getId().equals(locationId) || other.getId().equals(candidate.getId())) {
                    continue;
                }

                double distPR = calculateDistance(
                        newLocation.getLatitude(), newLocation.getLongitude(),
                        other.getLatitude(), other.getLongitude());

                double distQR = calculateDistance(
                        candidate.getLatitude(), candidate.getLongitude(),
                        other.getLatitude(), other.getLongitude());

                if (Math.max(distPR, distQR) < distPQ) {
                    isRNGNeighbor = false;
                    break;
                }
            }

            if (isRNGNeighbor) {
                edges.add(new RNGEdge(locationId, candidate.getId(), distPQ * 1000.0));
            }
        }

        batchCreateRelationships(edges);
        return edges.size();
    }

    public static class LocationWithDistance {
        private LocationNode location;
        private Double distance;

        public LocationWithDistance(LocationNode location, Double distance) {
            this.location = location;
            this.distance = distance;
        }

        public LocationNode getLocation() {
            return location;
        }

        public void setLocation(LocationNode location) {
            this.location = location;
        }

        public Double getDistance() {
            return distance;
        }

        public void setDistance(Double distance) {
            this.distance = distance;
        }
    }
}
