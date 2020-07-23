package twitter.producer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {
	
	Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
	
	List<String> terms = Lists.newArrayList("bitcoin");

	public TwitterProducer() {

	}

	public static void main(String[] args) {
		System.out.println("Twitter Producer");
		new TwitterProducer().run();
	}

	public void run() {

		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
		Client client = createTwitterClient(msgQueue);
		KafkaProducer<String, String> producer = createProducer();
		client.connect();
		
		Runtime.getRuntime().addShutdownHook(new Thread( () ->  {
			logger.info("Stopping application ... ");
			logger.info("Suttingdown client from twitter ... ");
			client.stop();
			logger.info("Closing producer ... ");
			producer.close();
			logger.info("Done ... ");
			
		}));
			
		
		while (!client.isDone()) {

			String msg = null;

			try {
				msg = msgQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				client.stop();
			}
			
			if(msg!=null)	{
				logger.info("Tweets is " + msg);
				
				
				producer.send(new ProducerRecord<String, String>("twitter_tweets", msg), new Callback()  {
					public void onCompletion(RecordMetadata metadata, Exception exception) {
						if(exception != null)	{
							logger.error("error received \n " + exception.getMessage());
						}
						
					}
				});
				
				
			}

		}
		
		logger.info("Applicaiton stopped");

	}

	String consumerKey = "E9o8RkVjD4ErP19V6cLkOdDS0";
	String consumerSecret = "rHAc4CPtvgmYZYe6VzM5f92Solv4rlPYKJatOqZh0avfb4ykiE";
	String token = "357345091-rObUxf0gVt7aGtNcmyyZS0IpAdkUKFdrztVN5Y6A";
	String secret = "7WnToF6Ovpw3VOUOWBwv0amlnAK1gJGhMg7GVPQeSah7b";

	public Client createTwitterClient(BlockingQueue<String> msgQueue) {

		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

		
		hosebirdEndpoint.trackTerms(terms);
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		ClientBuilder builder = new ClientBuilder().name("Hosebird-Client-01") // optional: mainly for the logs
				.hosts(hosebirdHosts).authentication(hosebirdAuth).endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue));

		Client hosebirdClient = builder.build();
		return hosebirdClient;
	}
	
	
	public KafkaProducer<String, String> createProducer()	{
		
		Properties props = new Properties();
		props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
		props.setProperty(ProducerConfig.RETRIES_CONFIG, String.valueOf(Integer.MAX_VALUE));
		props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
		
		props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));
		
		
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		
		return producer;
		
	}
	

}
