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
package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.AvtalRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;

@Transactional
@Api(value = "services anvandare", description = "REST API för testbarhet - Användare")
@RestController
@RequestMapping("/testability/useragreementtest")
@Profile({"dev", "testability-api"})
public class UserAgreementResource {

  @Autowired private AvtalRepository avtalRepository;

  @Autowired private GodkantAvtalRepository godkantAvtalRepository;

  @Autowired private AnvandarPreferenceRepository anvandarPreferenceRepository;

  @PutMapping("/godkannavtal/{hsaId}")
  public ResponseEntity<Void> godkannAvtal(@PathVariable("hsaId") String hsaId) {
    int avtalVersion = avtalRepository.getLatestAvtalVersion();
    godkantAvtalRepository.approveAvtal(hsaId, avtalVersion);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/avgodkannavtal/{hsaId}")
  public ResponseEntity<Void> avgodkannAvtal(@PathVariable("hsaId") String hsaId) {
    godkantAvtalRepository.removeAllUserApprovments(hsaId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/approvedTerms/{hsaId}")
  public boolean getTermsApproval(@PathVariable("hsaId") String hsaId) {
    return godkantAvtalRepository.userHasApprovedAvtal(
        hsaId, avtalRepository.getLatestAvtalVersion());
  }

  @DeleteMapping("/preferences/{hsaId}/{key}")
  public ResponseEntity<Void> deletePreference(
      @PathVariable("hsaId") String hsaId, @PathVariable("key") String key) {
    AnvandarPreference ap = anvandarPreferenceRepository.findByHsaIdAndKey(hsaId, key);
    if (ap != null) {
      anvandarPreferenceRepository.delete(ap);
    }
    return ResponseEntity.ok().build();
  }
}
