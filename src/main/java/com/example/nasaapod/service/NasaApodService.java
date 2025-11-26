//package com.example.nasaapod.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
//@Service
//public class NasaApodService {
//
//    private final RestTemplate restTemplate;
//    private final String apiKey = "cG9uM4Y9c3cbcckbJQude7yKope2jf7iuD8JXTXk";
//
//    public NasaApodService(RestTemplateBuilder builder) {
//        this.restTemplate = builder.build();
//    }
//
//    @Cacheable("apodCache")
//    public Map<String, Object> getTodayApod() {
//        String url = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;
//        return restTemplate.getForObject(url, Map.class);
//    }
//}


package com.example.nasaapod.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NasaApodService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public NasaApodService(RestTemplateBuilder builder,
                           @Value("${nasa.api.key}") String apiKey,
                           @Value("${nasa.api.url}") String apiUrl) {
        this.restTemplate = builder.build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    /**
     * Fetch today's APOD or the default APOD endpoint result.
     */
    public Map<String, Object> fetchTodayApod() {
        String url = apiUrl + "?api_key=" + apiKey;
        return callForSingleApod(url);
    }

    /**
     * Fetch APOD for a given date (YYYY-MM-DD).
     * This calls the same APOD endpoint with &date=...
     */
    public Map<String, Object> fetchApodByDate(String date) {
        // validate date format will be done by controller but still safe to use LocalDate parse here
        try {
            LocalDate.parse(date, ISO);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD");
        }
        String url = String.format("%s?api_key=%s&date=%s", apiUrl, apiKey, date);
        return callForSingleApod(url);
    }

    /**
     * Fetch recent N APODs (including today). Uses NASA's start_date/end_date range parameter.
     * NASA supports start_date and end_date and returns an array.
     */
    public List<Map<String, Object>> fetchRecentApods(int count) {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "count must be >= 1");
        }
        // limit count to a reasonable max to protect NASA API
        int maxCount = Math.min(count, 50);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(maxCount - 1); // inclusive

        String url = String.format("%s?api_key=%s&start_date=%s&end_date=%s",
                apiUrl, apiKey, start.format(ISO), end.format(ISO));

        try {
            ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, null, List.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                // NASA returns an array with oldest first (start_date -> end_date). We want most recent first.
                List<Map<String, Object>> list = new ArrayList<>();
                for (Object o : resp.getBody()) {
                    if (o instanceof Map) {
                        //noinspection unchecked
                        list.add((Map<String, Object>) o);
                    }
                }
                // reverse to make newest-first
                Collections.reverse(list);

                // Trim to requested count (in case API returned extra)
                if (list.size() > count) {
                    return list.subList(0, count);
                } else {
                    return list;
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "NASA API error");
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to contact NASA API: " + ex.getMessage());
        }
    }

    /* --- helper --- */
    private Map<String, Object> callForSingleApod(String url) {
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(url, Map.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                //noinspection unchecked
                return (Map<String, Object>) resp.getBody();
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "NASA API returned non-200");
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to contact NASA API: " + ex.getMessage());
        }
    }
}

