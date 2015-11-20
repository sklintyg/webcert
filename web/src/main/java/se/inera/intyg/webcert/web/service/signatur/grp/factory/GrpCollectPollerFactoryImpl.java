package se.inera.intyg.webcert.web.service.signatur.grp.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import se.inera.intyg.webcert.web.service.signatur.grp.GrpCollectPoller;

/**
 * Created by eriklupander on 2015-08-25.
 *
 * TODO. I'm not fond of this approach of getting hold of prototype-scoped
 * spring beans in a singleton context. Look into method injection as well.
 */
@Component
public class GrpCollectPollerFactoryImpl implements GrpCollectPollerFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public GrpCollectPoller getInstance() {
        return applicationContext.getBean("grpCollectPoller", GrpCollectPoller.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
