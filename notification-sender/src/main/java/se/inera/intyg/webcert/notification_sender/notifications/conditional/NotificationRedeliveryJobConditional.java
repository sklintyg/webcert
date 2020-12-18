/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.conditional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.Yaml;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.FeaturesConfiguration;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;

public class NotificationRedeliveryJobConditional implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRedeliveryJobConditional.class);

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {

        try {
            String featuresFile = "classpath:webcert/features.yaml"; // context.getEnvironment().getProperty("features.configuration.file");
            Assert.notNull(featuresFile, "Features configuration file must not be null");
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource(featuresFile);

            Yaml yaml = new Yaml();
            FeaturesConfiguration config = yaml.loadAs(resource.getInputStream(), FeaturesConfiguration.class);
            List<Feature> featureList = config.getFeatures().stream().map(Feature::new).collect(Collectors.toList());
            Map<String, Feature> map = AuthoritiesResolverUtil.toMap(featureList, Feature::getName);
            Feature feature = map.get(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);

            return (Optional.ofNullable(feature).filter(Feature::getGlobal).isPresent());

        } catch (IOException e) {
            LOG.error("Features configuration could not be loaded.", e);
            return true;
        }
    }
}
        /*
        SecurityConfigurationLoader configurationLoader = new SecurityConfigurationLoader("classpath:webcert/authorities.yaml",
            "classpath:webcert/features.yaml");
        configurationLoader.afterPropertiesSet();
        List<Feature> featureList = configurationLoader.getFeaturesConfiguration().getFeatures().stream().map(Feature::new).collect(
            Collectors.toList());
        Map<String, Feature> map = AuthoritiesResolverUtil.toMap(featureList, Feature::getName);
        Feature feature = map.get(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);

        return Optional.ofNullable(feature).filter(Feature::getGlobal).isPresent();
    }
}*/
