package com.parctice.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumerDemo {
	
	static Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

	public static void main(String[] args) {
		
		System.out.println("Consumer Demo");
		
		String groupId = "group1";
		
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		
		
		consumer.subscribe(Arrays.asList("first_topic"));
		
		while(true)	{
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			
			for(ConsumerRecord<String, String> record : records)	{
				logger.info("Received new metadata \n " +
						"Key: " + record.key() + "\n" +
						"Value: " + record.value() + "\n" +
						"Partition: " + record.partition() + "\n" +
						"Offset: " + record.offset()); 
			}
		}
		
		//consumer.close();
		
		
	}
}
