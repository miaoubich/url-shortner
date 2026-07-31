package org.miaoubich.kafka;

import org.miaoubich.config.KafkaTopicConfig;
import org.miaoubich.dto.ClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ClickEventProducer {

	private static final Logger log = LoggerFactory.getLogger(ClickEventProducer.class);
	
	private final KafkaTemplate<String, ClickEvent> kafkaTemplate;
	
	public ClickEventProducer(KafkaTemplate<String, ClickEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
	
	@Async
	public void publish(ClickEvent event) {
		kafkaTemplate.send(KafkaTopicConfig.CLICK_EVENTS_TOPIC, event.shortCode(), event)
			.whenComplete((result, ex) -> {
				if(ex != null) {
					log.warn("Failed to publish clickEvent for {}", event.shortCode(), ex);
				}
			});
	}
}
