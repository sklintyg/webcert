package se.inera.webcert.pu.stub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.population.residentmaster.v1.PersonpostTYPE;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PUBootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(PUBootstrapBean.class);

    @Autowired
    private LookupResidentForFullProfileWsStub stub;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapPersoner() throws IOException {
        List<Resource> files = getResourceListing("bootstrap-personer/*.json");
        LOG.debug("Bootstrapping {} personer for PU stub ...", files.size());
        for (Resource res : files) {
            addPersoner(res);
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
        PersonpostTYPE person = objectMapper.readValue(res.getFile(), PersonpostTYPE.class);
        stub.addUser(person);
        LOG.debug("Loaded person " + person.getPersonId());
    }
}
