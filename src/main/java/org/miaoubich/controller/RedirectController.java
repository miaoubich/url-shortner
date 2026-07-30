package org.miaoubich.controller;


import org.miaoubich.dto.RedirectTarget;
import org.miaoubich.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{4,12}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        RedirectTarget target = urlService.resolveLongUrl(shortCode);
        return ResponseEntity.status(302)
                .location(URI.create(target.longUrl()))
                .build();
    }
}
