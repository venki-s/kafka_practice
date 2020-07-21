package kakfa.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ProducerDemoCallback {

	public static void main(String[] args) {
		
		Logger logger = LoggerFactory.getLogger(ProducerDemoCallback.class);
		
		System.out.println("Hello producer");
		
		Properties props = new Properties();
		props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		
		for(int i=0;i<10;i++)	{
			ProducerRecord<String, String> record = 
					new ProducerRecord<String, String>("first_topic", "Hello world" + String.valueOf(i));
			
			producer.send(record, new Callback()  {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if(exception == null)	{
						logger.info("Received new metadata \n " +
									"Topic: " + metadata.topic() + "\n" +
									"Offset: " + metadata.offset() + "\n" +
									"Partition: " + metadata.partition() + "\n" +
									"Timestamp: " + metadata.timestamp()); 
					}
					else	{
						logger.error("error received \n " + exception.getMessage());
					}
					
				}
			});
		}
		
		
		
		
		producer.flush();
		
		producer.close();
		
	}

}
