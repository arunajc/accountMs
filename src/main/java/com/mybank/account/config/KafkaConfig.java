package com.mybank.account.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import com.mybank.account.model.TransactionDetails;

import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.spring.TracingProducerFactory;

@EnableKafka
@Configuration
public class KafkaConfig {

	@Value("${spring.application.name}")
	private String applicationName;

	@Component
	@ConfigurationProperties("mybank.kafka.producer")
	public static class KafkaConfigProps{

		private final Map<String, String> props = new HashMap<>();
		public Map<String, String> getProps(){
			return props;
		}
	}

	@Bean
	public Tracer tracer() {
		return io.jaegertracing.Configuration.fromEnv(applicationName) 
				.withSampler(
						io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
						.withType(ConstSampler.TYPE) 
						.withParam(1)) 
				.withReporter(
						io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
						.withLogSpans(true) 
						.withFlushInterval(1000) 
						.withMaxQueueSize(10000)) 
				.getTracer();
	}

	@Autowired
	KafkaConfigProps configProps;

	@Bean
	public ProducerFactory<String, TransactionDetails> producerFactory() {
		return new TracingProducerFactory<>(new DefaultKafkaProducerFactory<>((Map)configProps.getProps()), tracer());
	}

	@Bean
	KafkaTemplate<String, TransactionDetails> kafkaTemplate(){
		return new KafkaTemplate<>(producerFactory());
	}

}
