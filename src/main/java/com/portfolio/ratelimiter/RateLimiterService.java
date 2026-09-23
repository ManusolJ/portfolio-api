package com.portfolio.ratelimiter;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;

import com.portfolio.config.CacheProperties;

/** Holds one token bucket per client, evicting buckets that go unused. */
@Service
public class RateLimiterService {

    private final CacheProperties cacheProperties;
    private final RateLimitProperties rateLimitProperties;

    private final Cache<String, Bucket> buckets;

    public RateLimiterService(RateLimitProperties rateLimitProperties, CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
        this.rateLimitProperties = rateLimitProperties;
        buckets = Caffeine.newBuilder()
            .maximumSize(this.cacheProperties.maxBuckets())
            .expireAfterAccess(this.rateLimitProperties.refillInterval().multipliedBy(2))
            .build();
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(this.rateLimitProperties.capacity())
            .refillGreedy(this.rateLimitProperties.amount(), this.rateLimitProperties.refillInterval())
            .build();

        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket resolveBucket(String key) {
        return buckets.get(key, k -> createNewBucket());
    }

    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    public long getAvailableTokens(String key) {
        return resolveBucket(key).getAvailableTokens();
    }
}
