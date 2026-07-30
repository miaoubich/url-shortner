package org.miaoubich.service;

import java.time.Instant;

import org.miaoubich.dto.CreateUrlRequest;
import org.miaoubich.dto.RedirectTarget;
import org.miaoubich.dto.UrlResponse;
import org.miaoubich.entity.ShortUrl;
import org.miaoubich.repository.ShortUrlRepository;
import org.miaoubich.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final String LOCALHOST = "http://localhost:8080/";

    public UrlService(ShortUrlRepository shortUrlRepository, ShortCodeGenerator shortCodeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        String code;

        if (request.customCode() != null && !request.customCode().isBlank()) {
            if (shortUrlRepository.existsByShortCode(request.customCode())) {
                throw new IllegalArgumentException("Short code already in use: " + request.customCode());
            }
            code = request.customCode();
        } else {
            code = generateUniqueCode();
        }

        ShortUrl saved = shortUrlRepository.save(new ShortUrl(code, request.longUrl()));
        return toResponse(saved);
    }

    @Cacheable(cacheNames = CacheConfig.URL_LOOKUP_CACHE, key = "#shortCode")
    @Transactional(readOnly = true)
    public RedirectTarget resolveLongUrl(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("Unknown short code: " + shortCode));

        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            throw new EntityNotFoundException("Short code expired: " + shortCode);
        }
        return new RedirectTarget(shortUrl.getLongUrl());
    }

    @CacheEvict(cacheNames = CacheConfig.URL_LOOKUP_CACHE, key = "#shortCode")
    @Transactional
    public void deactivate(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndActiveTrue(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("Unknown short code: " + shortCode));
        shortUrl.setActive(false);
    }
    
    @CacheEvict(cacheNames = CacheConfig.URL_LOOKUP_CACHE, key = "#shortCode")
    @Transactional
    public void updateStatus(String shortCode, boolean active) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new EntityNotFoundException("Unknown short code: " + shortCode));
        shortUrl.setActive(active);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = shortCodeGenerator.generate();
        } while (shortUrlRepository.existsByShortCode(code));
        return code;
    }

    private UrlResponse toResponse(ShortUrl entity) {
        return new UrlResponse(
                entity.getShortCode(),
                LOCALHOST + entity.getShortCode(),
                entity.getLongUrl(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}