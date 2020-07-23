package com.parctice.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumerDemoAS {
	
	static Logger logger = LoggerFactory.getLogger(ConsumerDemoAS.class);

	public static void main(String[] args) {
		
		System.out.println("Consumer Demo");
		
		//String groupId = "group1";
		
		String topic = "first_topic";
		
		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		//props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		
		consumer.subscribe(Arrays.asList("first_topic"));
		
		
		TopicPartition partitiontoReadFrom = new TopicPartition(topic, 0);
		long offsetToReadFrom = 15l;
		consumer.assign(Arrays.asList(partitiontoReadFrom));
		
		consumer.seek(partitiontoReadFrom, offsetToReadFrom);
		
		int numberOfMessagesToRead = 5;
		boolean keepOnReading = true;
		int numberOfMessagesRead = 0;
		
		while(keepOnReading)	{
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			
			for(ConsumerRecord<String, String> record : records)	{
				numberOfMessagesRead = numberOfMessagesRead + 1;
				logger.info("Received new metadata \n " +
						"Key: " + record.key() + "\n" +
						"Value: " + record.value() + "\n" +
						"Partition: " + record.partition() + "\n" +
						"Offset: " + record.offset()); 
				
				if(numberOfMessagesRead >= numberOfMessagesToRead)	{
					keepOnReading = false;
					break;
				}
			}
		}
		
		//consumer.close();
		
		
	}
}
