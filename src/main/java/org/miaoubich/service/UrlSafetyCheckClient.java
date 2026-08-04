package org.miaoubich.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UrlSafetyCheckClient {

    private static final Logger log = LoggerFactory.getLogger(UrlSafetyCheckClient.class);
    private static final String USER_SAFETY_CHECK = "urlSafetyCheck";
    
    @CircuitBreaker(name = USER_SAFETY_CHECK, fallbackMethod = "fallbackAllow")
    @TimeLimiter(name = USER_SAFETY_CHECK)
    @Retry(name = USER_SAFETY_CHECK)
    public CompletableFuture<Boolean> isSafe(String longUrl) {
        return CompletableFuture.supplyAsync(() -> {
            if (longUrl.contains("force-circuit-open")) {
                throw new RuntimeException("Simulated safety-check outage");
            }
            boolean looksSuspicious = longUrl.contains("malware-test");
            return !looksSuspicious;
        });
    }

    /*
     * Resilience4j finding it "by name" has a strict rule attached: 
     *    the fallback method must accept the same parameters as the original method, 
     *    plus one extra Throwable at the end, and return the same type.
     * fallbackAllow(String longUrl, Throwable t) mirrors isSafe(String longUrl)
     *  — same first parameter, same return type (CompletableFuture<Boolean>), 
     *  with Throwable t appended.
     * */
    // Resilience4j calls fallbackAllow(...) automatically when isSafe() 
    //  fails/times out/exhausts retries.
    private CompletableFuture<Boolean> fallbackAllow(String longUrl, Throwable t) {
        log.warn("URL safety check unavailable, allowing by default: {}", t.toString());
        return CompletableFuture.completedFuture(true);
    }
}
