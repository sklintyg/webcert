package se.inera.webcert.hsa.stub;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import se.inera.webcert.hsa.model.Vardgivare;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapBean.class);

    @Autowired
    HsaServiceStub hsaServiceStub;

    @Autowired
    ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapVardgivare() throws IOException {
        
        List<Resource> files = getResourceListing("bootstrap-vardgivare/*.json");
        LOG.debug("Bootstrapping {} vardgivare for HSA stub ...", files.size());
        for (Resource res : files) {
            addVardgivare(res);
        }
        
        files = getResourceListing("bootstrap-medarbetaruppdrag/*.json");
        LOG.debug("Bootstrapping {} medarbetare for HSA stub ...", files.size());
        for (Resource res : files) {
            addPerson(res);
            addMedarbetaruppdrag(res);
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

    private void addMedarbetaruppdrag(Resource res) throws IOException {
        Medarbetaruppdrag medarbetaruppdrag = objectMapper.readValue(res.getFile(), Medarbetaruppdrag.class);
        hsaServiceStub.getMedarbetaruppdrag().add(medarbetaruppdrag);
    }
    
    private void addPerson(Resource res) throws IOException {
        HsaPerson hsaPerson = objectMapper.readValue(res.getFile(), HsaPerson.class);
        hsaServiceStub.addHsaPerson(hsaPerson);
    }

    private void addVardgivare(Resource res) throws IOException {
        Vardgivare vardgivare = objectMapper.readValue(res.getFile(), Vardgivare.class);
        hsaServiceStub.getVardgivare().add(vardgivare);
    }
}
