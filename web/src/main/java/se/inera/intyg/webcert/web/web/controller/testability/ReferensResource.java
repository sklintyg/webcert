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

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;

@RestController
@RequestMapping("/testability/referens")
@Profile({"dev", "testability-api"})
public class ReferensResource {

  public static final Logger LOG = LoggerFactory.getLogger(ReferensResource.class);

  @Autowired private ReferensRepository referensRepository;

  @PostMapping
  public ResponseEntity<Void> insertReferens(@RequestBody Referens referens) {
    Referens savedReferens = referensRepository.save(referens);
    LOG.info("Created Referens with id {} using testability API", savedReferens.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/referensCount")
  public Long getEventCountForCertificateIds(@RequestBody List<String> certificateIds) {
    final var referensList = (List<Referens>) referensRepository.findAll();
    return referensList.stream()
        .filter(referens -> certificateIds.contains(referens.getIntygsId()))
        .count();
  }

  @DeleteMapping
  public ResponseEntity<Void> deletReferenserByCertificateIds(
      @RequestBody List<String> certificateIds) {
    final var referensList = (List<Referens>) referensRepository.findAll();
    final var referensForDeletion =
        referensList.stream()
            .filter(referens -> certificateIds.contains(referens.getIntygsId()))
            .collect(Collectors.toList());
    referensRepository.deleteAll(referensForDeletion);
    LOG.info(
        "Deleted {} referenser based on certificateIds using testability API",
        referensForDeletion.size());

    return ResponseEntity.ok().build();
  }
}
