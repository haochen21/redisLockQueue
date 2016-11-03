package redisLockQueue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@PropertySource({ "classpath:/jms.properties" })
public class SendJmsConfig {

	@Autowired
	private Environment env;

	@Bean
	public ActiveMQConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
		activeMQConnectionFactory.setBrokerURL(env.getRequiredProperty("brokerURL"));
		return activeMQConnectionFactory;
	}

	@Bean
	public CachingConnectionFactory cachingConnectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		cachingConnectionFactory.setTargetConnectionFactory(connectionFactory());
		cachingConnectionFactory.setSessionCacheSize(1);
		return cachingConnectionFactory;
	}

	@Bean
	public JmsTemplate taskTemplate() {
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setDefaultDestination(new ActiveMQQueue(env.getRequiredProperty("taskDestination")));
		jmsTemplate.setConnectionFactory(cachingConnectionFactory());
		jmsTemplate.setTimeToLive(Long.parseLong(env.getRequiredProperty("timeToLive")));
		return jmsTemplate;
	}

	@Bean
	public SendTaskJms sendTaskJms() {
		SendTaskJms sendTaskJms = new SendTaskJms();
		sendTaskJms.setJmsTemplate(taskTemplate());
		return sendTaskJms;
	}

	@Bean
	public SendTaskTest sendTaskTest() {
		SendTaskTest sendTaskTest = new SendTaskTest();
		return sendTaskTest;
	}
}
