package se.inera.intyg.webcert.web.service.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.integreradenhet.model.IntegreradEnhet;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;

public class IntegreradeEnheterBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(IntegreradeEnheterBootstrapBean.class);

    @Autowired
    private IntegreradEnhetRepository integreradEnhetRepository;

    @PostConstruct
    public void initData() {
        List<Resource> files = getResourceListing("bootstrap-integrerade-enheter/*.json");
        for (Resource res : files) {
            LOG.debug("Loading resource " + res.getFilename());
            addIntegreradEnhet(res);
        }
    }

    private void addIntegreradEnhet(Resource res) {
        try {
            IntegreradEnhet integreradEnhet = new CustomObjectMapper().readValue(res.getInputStream(), IntegreradEnhet.class);
            integreradEnhetRepository.save(integreradEnhet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private List<Resource> getResourceListing(String classpathResourcePath) {
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            return Arrays.asList(r.getResources(classpathResourcePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
