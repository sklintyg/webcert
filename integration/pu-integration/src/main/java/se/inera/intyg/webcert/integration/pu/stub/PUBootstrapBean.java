package se.inera.intyg.webcert.integration.pu.stub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.riv.population.residentmaster.types.v1.ResidentType;

public class PUBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(PUBootstrapBean.class);

    @Autowired
    private ResidentStore residentStore;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapPersoner() throws IOException {
        List<Resource> files = getResourceListing("bootstrap-personer/*.json");
        LOG.debug("Bootstrapping {} personer for PU stub ...", files.size());
        for (Resource res : files) {
            try {
                addPersoner(res);
            } catch (Exception e) {
                LOG.error("PUBootstrap", e);
            }
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

    private void addPersoner(Resource res) throws IOException {
        LOG.debug("Loading personer from " + res.getFilename());
        ResidentType resident = objectMapper.readValue(res.getInputStream(), ResidentType.class);
        residentStore.addUser(resident);
        LOG.debug("Loaded person " + resident.getPersonpost().getPersonId());
    }
}
