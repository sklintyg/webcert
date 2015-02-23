package se.inera.webcert.notifications;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public final class TestDataUtil {

    private TestDataUtil() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(TestDataUtil.class);

    public static String readRequestFromFile(String filePath) {
        try {
            LOG.debug("Reading test data from: {}", filePath);
            ClassPathResource resource = new ClassPathResource(filePath);
            return IOUtils.toString(resource.getInputStream(), "UTF-8");
        } catch (IOException e) {
            LOG.error("Could not read test data from: {}, error {}", filePath, e.getMessage());
            return null;
        }
   }
}
