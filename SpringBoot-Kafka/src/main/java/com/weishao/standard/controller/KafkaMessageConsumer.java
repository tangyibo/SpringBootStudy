package com.weishao.standard.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaMessageConsumer {

	private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);

	@KafkaListener(topics = { "${app.kafka.topic.name}" }, containerFactory = "TaskKafkaListenerContainerFactory")
	public void receive(ConsumerRecord<String, String> record, Acknowledgment ack) {
		// submit offset
		ack.acknowledge();

		try {
			//handle message
			logger.info(record.value().toString());
		} catch (Exception e) {
			logger.error("Error when handle kafka message:", e);
		}

	}
}