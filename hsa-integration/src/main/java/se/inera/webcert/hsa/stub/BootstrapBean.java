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

        LOG.debug("Bootstrapping vardgivare for HSA stub ...");
        List<Resource> files = getResourceListing("bootstrap-vardgivare/*.json");
        for (Resource res : files) {
            addVardgivare(res);
        }

        LOG.debug("Bootstrapping medarbetare for HSA stub ...");
        files = getResourceListing("bootstrap-medarbetaruppdrag/*.json");
        for (Resource res : files) {
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
        LOG.debug("Loading medarbetaruppdrag from " + res.getFilename());
        Medarbetaruppdrag medarbetaruppdrag = objectMapper.readValue(res.getFile(), Medarbetaruppdrag.class);
        hsaServiceStub.getMedarbetaruppdrag().add(medarbetaruppdrag);
        LOG.debug("Loaded medarbetaruppdrag for " + medarbetaruppdrag.getHsaId());
    }

    private void addVardgivare(Resource res) throws IOException {
        Vardgivare vardgivare = objectMapper.readValue(res.getFile(), Vardgivare.class);
        hsaServiceStub.getVardgivare().add(vardgivare);
        LOG.debug("Loaded vardgivare " + vardgivare.getId());
    }
}
