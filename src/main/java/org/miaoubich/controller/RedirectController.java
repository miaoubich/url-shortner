package org.miaoubich.controller;


import java.net.URI;
import java.time.Instant;

import org.miaoubich.dto.ClickEvent;
import org.miaoubich.dto.RedirectTarget;
import org.miaoubich.kafka.ClickEventProducer;
import org.miaoubich.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private final UrlService urlService;
    private final ClickEventProducer clickEventProducer;

    public RedirectController(UrlService urlService, ClickEventProducer clickEventProducer) {
        this.urlService = urlService;
        this.clickEventProducer = clickEventProducer;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{4,12}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        RedirectTarget target = urlService.resolveLongUrl(shortCode);
        
        clickEventProducer.publish(new ClickEvent(shortCode, Instant.now()));
        
        return ResponseEntity.status(302)
                .location(URI.create(target.longUrl()))
                .build();
    }
}
