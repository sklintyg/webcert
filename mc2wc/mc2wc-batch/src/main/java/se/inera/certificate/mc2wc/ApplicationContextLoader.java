package se.inera.certificate.mc2wc;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertySource;

public class ApplicationContextLoader {

	protected ConfigurableApplicationContext applicationContext;

    public ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Loads application context. Override this method to change how the
     * application context is loaded.
     * 
     * @param configLocations
     *            configuration file locations
     */
    protected void loadApplicationContext(PropertySource ps, String[] configLocations) {
        applicationContext = new ClassPathXmlApplicationContext(configLocations, false);
        applicationContext.getEnvironment().getPropertySources().addFirst(ps);
        applicationContext.getEnvironment().setActiveProfiles("prod");
        applicationContext.registerShutdownHook();
        applicationContext.refresh();
    }

    /**
     * Injects dependencies into the object. Override this method if you need
     * full control over how dependencies are injected.
     * 
     * @param main
     *            object to inject dependencies into
     */
    protected void injectDependencies(Object main) {
        getApplicationContext().getBeanFactory().autowireBeanProperties(
                main, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
    }

    /**
     * Loads application context, then injects dependencies into the object.
     * 
     * @param main
     *            object to inject dependencies into
     * @param configLocations
     *            configuration file locations
     */
    public void load(Object main, PropertySource ps, String... configLocations) {
        loadApplicationContext(ps,configLocations);
        injectDependencies(main);
    }

}
