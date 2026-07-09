/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.csintegration.certificate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetUnitNotificationConfig {

  @Value("${unit.notification.config.path:}")
  private String unitNotificationConfigPath;

  private List<RegionNotificationConfig> integratedUnitNotificationConfig;

  private final JsonMapper jsonMapper;

  public List<RegionNotificationConfig> get() {
    if (integratedUnitNotificationConfig == null) {
      integratedUnitNotificationConfig = new ArrayList<>();
      try (final var resourceAsStream = new FileInputStream(unitNotificationConfigPath)) {
        integratedUnitNotificationConfig =
            jsonMapper.readValue(resourceAsStream, new TypeReference<>() {});
        log.info(
            "Integrated Unit Notification was loaded with configuration: {}",
            integratedUnitNotificationConfig);
      } catch (FileNotFoundException e) {
        log.warn(
            "File not found: {}. Returning empty configuration.", unitNotificationConfigPath, e);
      } catch (Exception e) {
        log.error(
            String.format(
                "Failed to load Integrated Unit Notification configuration. Reason: %s",
                e.getMessage()),
            e);
      }
    }
    return integratedUnitNotificationConfig;
  }
}
