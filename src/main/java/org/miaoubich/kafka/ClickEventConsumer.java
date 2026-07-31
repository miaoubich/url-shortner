package org.miaoubich.kafka;

import org.miaoubich.config.KafkaTopicConfig;
import org.miaoubich.dto.ClickEvent;
import org.miaoubich.repository.ClickStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ClickEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);

    private final ClickStatsRepository clickStatsRepository;

    public ClickEventConsumer(ClickStatsRepository clickStatsRepository) {
        this.clickStatsRepository = clickStatsRepository;
    }

    @KafkaListener(topics = KafkaTopicConfig.CLICK_EVENTS_TOPIC, groupId = "analytics-service")
    public void onClickEvent(ClickEvent event) {
        log.debug("Processing click event for {}", event.shortCode());
        clickStatsRepository.incrementClicks(event.shortCode(), event.clickedAt());
    }
}
