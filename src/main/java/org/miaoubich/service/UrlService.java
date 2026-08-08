package org.miaoubich.service;

import java.time.Instant;
import java.util.List;

import org.miaoubich.config.CacheConfig;
import org.miaoubich.dto.CreateUrlRequest;
import org.miaoubich.dto.PagedResponse;
import org.miaoubich.dto.RedirectTarget;
import org.miaoubich.dto.UrlResponse;
import org.miaoubich.entity.ShortUrl;
import org.miaoubich.exception.UnsafeUrlException;
import org.miaoubich.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UrlService {

	private final Logger log = LoggerFactory.getLogger(UrlService.class);
	
    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlSafetyCheckClient safetyCheckClient;
    // To verify Spring did switch to Virtual threads 
//    private final ObjectProvider<TaskExecutor> executor;
    
    private final String LOCALHOST = "http://localhost:8080/";

    public UrlService(ShortUrlRepository shortUrlRepository, 
    		          ShortCodeGenerator shortCodeGenerator,
    		          UrlSafetyCheckClient safetyCheckClient) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.safetyCheckClient = safetyCheckClient;
//        this.executor= executor;
    }
    
//    @PostConstruct
    public void checkExecutor() {
    	/*
    	 * Spring Boot 4.x does not use virtual threads yet, it will print 
    	 * executor -> class org.springframework.core.task.SimpleAsyncTaskExecutor
    	 * 
    	 * With Spring Boot 3.3.x+, it will print, stable virtual thread support. 
    	 * executor -> class org.springframework.core.task.VirtualThreadTaskExecutor
		 *  
    	 * */
//        log.info("executor -> {}", executor.getObject().getClass());
    }
    
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
    	/* 
    	 * Fire the safety check; fails open per our design decision above.
         * We don't block on the result here since isSafe() returns a CompletableFuture
         */
        boolean safe = safetyCheckClient.isSafe(request.longUrl()).join();
        
        if (!safe) {
            throw new IllegalArgumentException("This URL was flagged as potentially unsafe: " + request.longUrl());
        }
        
    	String code;

        if (request.customCode() != null && !request.customCode().isBlank()) {
            if (shortUrlRepository.existsByShortCode(request.customCode())) {
                throw new UnsafeUrlException("Short code already in use: " + request.customCode());
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
    
    @Transactional(readOnly = true)
    public PagedResponse<UrlResponse> listUrls(Long cursor, int pageSize) {
        long effectiveCursor = cursor != null ? cursor : Long.MAX_VALUE;

        // Fetch one extra row beyond the page size — its presence tells us
        //  whether there's a next page, without a separate COUNT query.
        List<ShortUrl> rows = shortUrlRepository.findPageByCursor(effectiveCursor, Limit.of(pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<ShortUrl> page = hasMore ? rows.subList(0, pageSize) : rows;

        Long nextCursor = hasMore ? page.get(page.size() - 1).getId() : null;
        List<UrlResponse> items = page.stream().map(this::toResponse).toList();

        return new PagedResponse<>(items, nextCursor, hasMore);
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