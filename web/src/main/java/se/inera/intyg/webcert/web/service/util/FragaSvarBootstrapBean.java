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

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

public class FragaSvarBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarBootstrapBean.class);

    @Autowired
    private FragaSvarRepository fragaSvarRepository;

    @PostConstruct
    public void initData() {

        List<Resource> files = getResourceListing("bootstrap-fragasvar/*.json");
        for (Resource res : files) {
            LOG.debug("Loading resource " + res.getFilename());
            addFragaSvar(res);
        }
    }

    private void addFragaSvar(Resource res) {

        try {
            FragaSvar fragaSvar = new CustomObjectMapper().readValue(res.getInputStream(), FragaSvar.class);
            fragaSvarRepository.save(fragaSvar);
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
