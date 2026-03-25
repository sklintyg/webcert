/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.infra.security.authorities.bootstrap;

import java.io.IOException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesConfiguration;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesException;
import se.inera.intyg.webcert.infra.security.authorities.FeaturesConfiguration;

/**
 * The security configuration is read from two seperate YAML files which are injected into the
 * constructor upon creating an object of this class.
 *
 * <p>The YAML files are parsed and the resulting configuration can be fetched by calling the {@link
 * SecurityConfigurationLoader#getAuthoritiesConfiguration()} and {@link
 * SecurityConfigurationLoader#getFeaturesConfiguration()} method.
 */
@Component("AuthoritiesConfigurationLoader")
public class SecurityConfigurationLoader extends YamlPropertiesFactoryBean
    implements InitializingBean {

  @Value("${max.aliases.for.collections:500}")
  private Integer maxAliasesForCollections;

  @Value("${authorities.configuration.file}")
  private String authoritiesConfigurationFile;

  @Value("${features.configuration.file}")
  private String featuresConfigurationFile;

  private AuthoritiesConfiguration authoritiesConfiguration;
  private FeaturesConfiguration featuresConfiguration;

  private SecurityConfigurationLoader() {
    // Uses @Value injected authoritiesConfigurationFile
  }

  /**
   * Constructor taking a path to the authorities configuration file.
   *
   * @param authoritiesConfigurationFile path to YAML configuration file
   */
  public SecurityConfigurationLoader(
      String authoritiesConfigurationFile,
      String featuresConfigurationFile,
      Integer maxAliasesForCollections) {
    Assert.notNull(authoritiesConfigurationFile, "Authorities configuration file must not be null");
    Assert.notNull(featuresConfigurationFile, "Features configuration file must not be null");
    Assert.notNull(
        maxAliasesForCollections, "Configuration maxAliasesForCollections must not be null");
    this.authoritiesConfigurationFile = authoritiesConfigurationFile;
    this.featuresConfigurationFile = featuresConfigurationFile;
    this.maxAliasesForCollections = maxAliasesForCollections;
  }

  /**
   * Invoked by a BeanFactory after it has set all bean properties supplied (and satisfied
   * BeanFactoryAware and ApplicationContextAware).
   *
   * <p>This method allows the bean instance to perform initialization only possible when all bean
   * properties have been set and to throw an exception in the event of misconfiguration.
   *
   * @throws AuthoritiesException in the event of misconfiguration (such as failure to set an
   *     essential property) or if initialization fails.
   */
  @Override
  public void afterPropertiesSet() throws AuthoritiesException {
    authoritiesConfiguration =
        loadConfiguration(authoritiesConfigurationFile, AuthoritiesConfiguration.class);
    featuresConfiguration =
        loadConfiguration(featuresConfigurationFile, FeaturesConfiguration.class);
  }

  /** Gets the loaded authorities configuration. */
  public AuthoritiesConfiguration getAuthoritiesConfiguration() {
    return this.authoritiesConfiguration;
  }

  /** Gets the loaded features configuration. */
  public FeaturesConfiguration getFeaturesConfiguration() {
    return this.featuresConfiguration;
  }

  private Resource getResource(String location) {
    final String url = ResourceUtils.isUrl(location) ? location : "file:" + location;
    PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
    return r.getResource(url);
  }

  private <T> T loadConfiguration(String location, Class<T> type) {
    final var loaderOptions = new LoaderOptions();
    loaderOptions.setMaxAliasesForCollections(maxAliasesForCollections);
    Yaml yaml = new Yaml(loaderOptions);
    try {
      return yaml.loadAs(getResource(location).getInputStream(), type);
    } catch (IOException e) {
      throw new AuthoritiesException("Could not load configuration file: " + location, e);
    }
  }
}
