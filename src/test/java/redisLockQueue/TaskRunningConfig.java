package redisLockQueue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

import redisLockQueue.config.ServiceConfig;

@Configuration
@Import({ ServiceConfig.class })
@PropertySource({ "classpath:/jms.properties" })
public class TaskRunningConfig {

	@Autowired
	private Environment env;

	@Bean
	public ActiveMQConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(env.getRequiredProperty("brokerURL"));
		activeMQConnectionFactory.setTrustAllPackages(true);
		return activeMQConnectionFactory;
	}

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		cachingConnectionFactory.setTargetConnectionFactory(connectionFactory());
		cachingConnectionFactory.setSessionCacheSize(1);
		return cachingConnectionFactory;
	}

	@Bean()
	public TaskRunningTest taskRunningTest() {
		TaskRunningTest test = new TaskRunningTest();
		return test;
	}

	@Bean
	public SimpleMessageListenerContainer taskMessageListenerContainer() {
		SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer();
		messageListenerContainer.setConnectionFactory(cachingConnectionFactory());
		messageListenerContainer.setDestination(new ActiveMQQueue(env.getRequiredProperty("taskDestination")));
		messageListenerContainer.setMessageListener(taskRunningTest());
		messageListenerContainer.setConcurrentConsumers(1);
		return messageListenerContainer;
	}
}
