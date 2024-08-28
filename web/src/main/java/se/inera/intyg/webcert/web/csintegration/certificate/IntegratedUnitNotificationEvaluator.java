/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegratedUnitNotificationEvaluator {
   private static final String JSON_SOURCE = "";


   public boolean mailNotification(String careProviderId, String issuedOnUnitId) {
      final var objectMapper = new ObjectMapper();
      try {
         final var jsonMap = objectMapper.readValue(JSON_SOURCE,
             new TypeReference<Map<String, List<IntegratedUnitNotificationConfig>>>() {
             });
         return evaluateMailNotification(careProviderId, issuedOnUnitId, jsonMap);
      } catch (Exception e) {
         throw new IllegalStateException("Error trying to parse integrated unit json configuration file");
      }
   }

   private boolean evaluateMailNotification(String careProviderId, String issuedOnUnitId, Map<String, List<IntegratedUnitNotificationConfig>> configMap) {
      return configMap.values().stream()
          .flatMap(List::stream)
          .anyMatch(config ->
                  evaluateMailNotification(
                      config.getIssuedOnUnit(),
                      config.getCareProviders(),
                      config.getDatetime(),
                      issuedOnUnitId,
                      careProviderId
                  )
              );
   }

   private boolean evaluateMailNotification(List<String> unitIds, List<String> careProviderIds, LocalDateTime activateFrom, String unitId, String careProviderId) {
      if (!careProviderIds.contains(careProviderId)) {
         return false;
      }

      if (!unitIds.contains(unitId)) {
         return false;
      }

     return !activateFrom.isAfter(LocalDateTime.now());
   }
}
