//package com.example.nasaapod.controller;
//
//import com.example.nasaapod.service.NasaApodService;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
//@RestController
//@RequestMapping("/api/apod")
//public class ApodController {
//
//    private final NasaApodService service;
//
//    public ApodController(NasaApodService service) {
//        this.service = service;
//    }
//
//    @GetMapping("/today")
//    public Map<String, Object> getTodayApod() {
//        return service.getTodayApod();
//    }
//}

package com.example.nasaapod.controller;

import com.example.nasaapod.service.NasaApodService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apod")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class ApodController {

    private final NasaApodService nasaApodService;

    public ApodController(NasaApodService nasaApodService) {
        this.nasaApodService = nasaApodService;
    }

    @GetMapping(value = "/today", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value = "apodCache", key = "'today'")
    public Map<String, Object> today() {
        return nasaApodService.fetchTodayApod();
    }

    /**
     * GET /api/apod?date=YYYY-MM-DD
     * Example: /api/apod?date=2025-11-25
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value = "apodByDate", key = "#date")
    public Map<String, Object> byDate(@RequestParam(name = "date") String date) {
        return nasaApodService.fetchApodByDate(date);
    }

    /**
     * GET /api/apod/recent?count=10
     * Example: /api/apod/recent?count=10
     */
    @GetMapping(value = "/recent", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(value = "apodRecent", key = "#count")
    public List<Map<String, Object>> recent(@RequestParam(name = "count", defaultValue = "10") int count) {
        return nasaApodService.fetchRecentApods(count);
    }
}
