package com.example.nasaapod.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        var cache = new ConcurrentMapCache("apodCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(50)
                        .build()
                        .asMap(),
                false
        );

        var manager = new org.springframework.cache.support.SimpleCacheManager();
        manager.setCaches(List.of(cache));
        return manager;
    }
}

//package com.example.nasaapod.config;
//
//import com.github.benmanes.caffeine.cache.Caffeine;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//
//import org.springframework.cache.caffeine.CaffeineCache;
//import org.springframework.cache.caffeine.CaffeineCacheManager;
//import org.springframework.cache.support.SimpleCacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
////import java.util.concurrent.TimeUnit;
//
//@Configuration
//@EnableCaching
//public class CacheConfig {
//
//    @Bean
//    public Caffeine<Object, Object> caffeineConfig() {
//        return Caffeine.newBuilder()
//                .expireAfterWrite(10, TimeUnit.MINUTES)
//                .maximumSize(100);
//    }
//
//    @Bean
//    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
//
//        CaffeineCache apodToday = new CaffeineCache("apodToday", caffeine.build());
//        CaffeineCache apodByDate = new CaffeineCache("apodByDate", caffeine.build());
//        CaffeineCache apodRange = new CaffeineCache("apodRange", caffeine.build());
//
//        SimpleCacheManager manager = new SimpleCacheManager();
//        manager.setCaches(List.of(
//                apodToday,
//                apodByDate,
//                apodRange
//        ));
//
//        return manager;
//    }
//}
//
