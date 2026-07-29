package org.miaoubich.controller;

import org.miaoubich.dto.CreateUrlRequest;
import org.miaoubich.dto.RedirectTarget;
import org.miaoubich.dto.UrlResponse;
import org.miaoubich.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/urls")
    public ResponseEntity<UrlResponse> createUrl(@Valid @RequestBody CreateUrlRequest request) {
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity
                .created(URI.create("/urls/" + response.shortCode()))
                .body(response);
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{4,12}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        RedirectTarget target = urlService.resolveLongUrl(shortCode);
        return ResponseEntity.status(302)
                .location(URI.create(target.longUrl()))
                .build();
    }
}