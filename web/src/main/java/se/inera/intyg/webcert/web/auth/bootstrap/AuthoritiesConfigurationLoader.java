package se.inera.intyg.webcert.web.auth.bootstrap;

import static java.lang.String.format;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConfiguration;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 *  The authorities configuration is read from a YAML file which is
 *  injected into the constructor upon creating an object of this class.
 *
 *  The YAML file is parsed and the resulting configuration can be fetched
 *  by calling the getConfiguration() method.
 */
@Component("AuthoritiesConfigurationLoader")
public class AuthoritiesConfigurationLoader implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritiesConfigurationLoader.class);

    private String authoritiesConfigurationFile;

    private AuthoritiesConfiguration authoritiesConfiguration;


    /**
     * Constructor taking a path to the authorities configuration file.
     *
     * @param authoritiesConfigurationFile path to YAML configuration file
     */
    public AuthoritiesConfigurationLoader(String authoritiesConfigurationFile) {
        Assert.notNull(authoritiesConfigurationFile, "Authorities configuration file must not be null");
        this.authoritiesConfigurationFile = authoritiesConfigurationFile;
    }


    // ~ Public scope
    // ======================================================================================================

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Resource resource = getResource(authoritiesConfigurationFile);
        URI uri = null;

        try {
            uri = resource.getURI();
            authoritiesConfiguration = loadConfiguration(Paths.get(resource.getURI()));
            System.err.println(authoritiesConfiguration);
        } catch (IOException ioe) {
           throw new AuthoritiesException(format("Could not load authorities configuration file %s", uri.getPath()), ioe);
        }

    }

    /**
     * Gets the loaded authorities configuration.
     *
     * @return
     */
    public AuthoritiesConfiguration getConfiguration() {
        return this.authoritiesConfiguration;
    }


    // ~ Private scope
    // ======================================================================================================

    private Resource getResource(String location) {
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        return r.getResource(location);
    }

    private AuthoritiesConfiguration loadConfiguration(Path path) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(path)) {
            AuthoritiesConfiguration ac = yaml.loadAs(in, AuthoritiesConfiguration.class);
            return ac;
        }
    }

}

