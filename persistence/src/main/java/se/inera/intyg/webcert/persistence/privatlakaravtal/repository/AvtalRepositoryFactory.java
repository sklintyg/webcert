package se.inera.intyg.webcert.persistence.privatlakaravtal.repository;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
@Service
public class AvtalRepositoryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AvtalRepositoryFactory.class);

    @Value("${privatepractitioner.defaultterms.file}")
    private String fileUrl;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private AvtalRepository avtalRepository;

    @PostConstruct
    public void populateStandardAvtal() {

        Integer latestAvtalVersion = avtalRepository.getLatestAvtalVersion();
        if (latestAvtalVersion == -1) {
            try {
                Resource resource = resourceLoader.getResource(fileUrl);

                if (!resource.exists()) {
                    LOG.error("Could not read privatlakare avtal file since the resource '{}' does not exist", fileUrl);
                    return;
                }

                String avtalText = IOUtils.toString(resource.getInputStream(), "UTF-8");
                Avtal avtal = new Avtal();
                avtal.setAvtalText(avtalText);
                avtal.setAvtalVersion(1);
                avtal.setVersionDatum(LocalDateTime.now());
                avtalRepository.save(avtal);
                LOG.info("Persisted basic Avtal for privatlakare.");

            } catch (IOException ioe) {
                LOG.error("Something went wrong while persisting basic privatlakaravtal on startup. Message: " + ioe.getMessage());
                throw new RuntimeException(ioe);
            }
        }
    }
}
