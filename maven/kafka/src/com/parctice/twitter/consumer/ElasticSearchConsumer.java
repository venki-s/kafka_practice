package com.parctice.twitter.consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import com.google.gson.JsonParser;



public class ElasticSearchConsumer {

	public static void main(String[] args) throws IOException {
		
		RestHighLevelClient client = null;
		KafkaConsumer<String, String> consumer = null;
		
		
		try	{
			
			client = createClient();
			consumer = createConsumer();
			
			while(true)	{
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
				
				for(ConsumerRecord<String, String> record : records)	{
					
					/*
					 * id strategy to make consumer idempotent
					 * 
					 * 1. id = record.topic() + "-" + record.partition() + "-" + record.offset();
					 * 2. id = twitter specific id
					 * 
					 * 
					 */
					
					String id = extractId(record.value());
					
					IndexRequest indexRequest = new IndexRequest("twitter", "tweets", id).source(record.value(), XContentType.JSON);
					IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
					System.out.println("id is " + indexResponse.getId());
					
					try	{
						Thread.sleep(2000);
					}
					catch(Exception e)	{
						e.printStackTrace();
					}
					
				}
			}
		}
		catch(Exception e)	{
			e.printStackTrace();
		}
		finally	{
			consumer.close();
			client.close();
		}


	}

	public static RestHighLevelClient createClient() {

		String hostname = "localhost";
		String username = "";
		String password = "";

		final CredentialsProvider cp = new BasicCredentialsProvider();
		cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

		RestClientBuilder rcBuilder = RestClient.builder(new HttpHost(hostname, 9200, "http"))
				.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

			
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setDefaultCredentialsProvider(cp);
					}
				});
		
		
		RestHighLevelClient client = new RestHighLevelClient(rcBuilder);
		

		return client;
	}
	
	public static KafkaConsumer<String, String> createConsumer()	{
		

		Properties props = new Properties();
		props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "twitter-group");
		props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Arrays.asList("twitter_tweets"));
		
		return consumer;
		
	}
	
	private static JsonParser parser = new JsonParser();
	
	private static String extractId(String tweetJson)	{
		
		return parser.parse(tweetJson)
				.getAsJsonObject()
				.get("id_str")
				.getAsString();
		
	}
	
	

}
