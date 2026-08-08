package org.miaoubich.controller;

import java.net.URI;

import org.miaoubich.dto.CreateUrlRequest;
import org.miaoubich.dto.PagedResponse;
import org.miaoubich.dto.UpdateUrlStatusRequest;
import org.miaoubich.dto.UrlResponse;
import org.miaoubich.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;
    private final String URLS = "/urls/";

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity
                .created(URI.create(URLS + response.shortCode()))
                .body(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deactivate(@PathVariable String shortCode) {
        urlService.deactivate(shortCode);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{shortCode}")
    public ResponseEntity<Void> updateStatus(@PathVariable String shortCode,
    										 @Valid @RequestBody UpdateUrlStatusRequest request){
    	urlService.updateStatus(shortCode, request.active());
    	return ResponseEntity.noContent().build();
    }
    
    @GetMapping
    public PagedResponse<UrlResponse> listUrls(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int pageSize) {
    	/*
    	 * This is a small but real defensive measure — without it, 
    	 * a client could request ?pageSize=1000000 and force a massive query. 
    	 * Capping it server-side means the client's input is a request, 
    	 * not a command — you decide the actual ceiling, regardless of what they ask for.
    	 * */
        int cappedPageSize = Math.min(pageSize, 100);
        return urlService.listUrls(cursor, cappedPageSize);
    }
}