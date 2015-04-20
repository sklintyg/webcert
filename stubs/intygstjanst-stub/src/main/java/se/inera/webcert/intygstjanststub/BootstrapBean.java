package se.inera.webcert.intygstjanststub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BootstrapBean {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapBean.class);

    @Autowired
    private IntygStore intygStore;

    private JAXBContext jaxbContext;

    @PostConstruct
    public void initData() {
        try {
            LOG.debug("Intygstjanst Stub : initializing intyg data...");
            jaxbContext = JAXBContext.newInstance(GetCertificateForCareResponseType.class);

            List<Resource> files = getResourceListing("bootstrap-intyg/*.xml");
            for (Resource res : files) {
                addIntyg(res);
            }

        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Could not bootstrap intygsdata for intygstjanststub", e);
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

    private void addIntyg(Resource res) throws JAXBException, IOException {
        GetCertificateForCareResponseType response = jaxbContext.createUnmarshaller().unmarshal(new StreamSource(res.getInputStream()),
                GetCertificateForCareResponseType.class).getValue();

        intygStore.addIntyg(response);
    }

}
