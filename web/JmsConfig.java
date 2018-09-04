package se.inera.intyg.webcert.web.config;

//@Configuration(value = "internalJmsConfig")
//@EnableJms
public class JmsConfig {

    // @Value("${activemq.broker.url}")
    // private String brokerUrl;
    //
    // @Value("${activemq.broker.username}")
    // private String brokerUsername;
    //
    // @Value("${activemq.broker.password}")
    // private String brokerPassword;
    //
    // @Value("${activemq.internal.notification.queue.name}")
    // private String internalNotificationQueue;
    //
    // @PostConstruct
    // public void init() {
    // System.out.println("HEJ HEJ");
    // }
    //
    // @Bean
    // public ConnectionFactory connectionFactory() {
    // return new PooledConnectionFactory(new ActiveMQConnectionFactory(brokerPassword, brokerUsername, brokerUrl));
    // }
    //
    // @Bean
    // public JmsTransactionManager jmsTransactionManager() {
    // return new JmsTransactionManager(connectionFactory());
    // }
    //
    // @Bean(value = "internalNotificationJmstemplate")
    // public JmsTemplate jmsTemplate() {
    // final JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
    // jmsTemplate.setDefaultDestination(internalNotificationQueue());
    // jmsTemplate.setSessionTransacted(true);
    // return jmsTemplate;
    // }
    //
    // @Bean(value = "internalNotificationQueue")
    // public Queue internalNotificationQueue() {
    // return new ActiveMQQueue(internalNotificationQueue);
    // }
}
