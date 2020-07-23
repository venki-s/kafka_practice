package com.parctice.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsumerDemoGroupsThread {
	
	static Logger logger = LoggerFactory.getLogger(ConsumerDemoGroupsThread.class);

	public static void main(String[] args) {
		
		System.out.println("Consumer Demo");
		
		new ConsumerDemoGroupsThread().run();
		
	}
	
	private ConsumerDemoGroupsThread()	{
		
	}
	
	private void run()	{
		
		final Logger logger1 = LoggerFactory.getLogger(ConsumerDemoGroupsThread.class);
		
		CountDownLatch latch = new CountDownLatch(1);
		
		logger1.info("Creating a consumer thread");
		
		Runnable consumerRunnable = new ConsumerThread(latch);
		
		Thread myThread = new Thread(consumerRunnable);
		
		myThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() ->	{
			logger1.info("Caught shutdown hook");
			((ConsumerThread)consumerRunnable).shutdown();
			
			try	{
				latch.await();
			}
			catch(InterruptedException ie)	{
				logger.info("Thread interruppted " + ie.getMessage());
			}
			finally	{
				logger.info("Applicaton is closing");
			}
			
		}));
		
		
		try	{
			latch.await();
		}
		catch(InterruptedException ie)	{
			logger.info("Thread interruppted " + ie.getMessage());
		}
		finally	{
			logger.info("Applicaton is closing");
		}
		
	}
	
	public class ConsumerThread implements Runnable	{
		
		private CountDownLatch latch;
		KafkaConsumer<String, String> consumer = null;
		
		Logger logger = LoggerFactory.getLogger(ConsumerThread.class);
		
		public ConsumerThread(CountDownLatch latch)	{
			this.latch = latch;
			
			String groupId = "group4";
			
			Properties props = new Properties();
			props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
			props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			
			consumer = new KafkaConsumer<String, String>(props);
			consumer.subscribe(Arrays.asList("first_topic"));
		}

		@Override
		public void run() {
			
			try	{
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
			}
			catch(WakeupException we)	{
				logger.info("received shutdown " + we.getMessage());
			}
			finally	{
				consumer.close();
				latch.countDown();
			}
			
			
			
		}
		
		public void shutdown()	{
			consumer.wakeup();
		}
		
		
		
	}
}
