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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.web.controller.testability.dto.ArendeAffectedResponse;
import se.inera.intyg.webcert.web.web.controller.testability.dto.SimpleArende;

@Transactional
@Api(value = "services arende", description = "REST API för testbarhet - Ärenden")
@RestController
@RequestMapping("/testability/arendetest")
@Profile({"dev", "testability-api"})
public class ArendeResource {

  @PersistenceContext private EntityManager entityManager;

  private TransactionTemplate transactionTemplate;

  @Autowired
  public void setTxManager(PlatformTransactionManager transactionManager) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @Autowired private ArendeRepository arendeRepository;

  @Autowired private ArendeDraftRepository arendeDraftRepository;

  @GetMapping("/intyg/{intygsId}")
  public ResponseEntity<List<String>> getStalldaFragor(@PathVariable("intygsId") String intygsId) {
    List<Arende> byIntygsId = arendeRepository.findByIntygsId(intygsId);
    return ResponseEntity.ok(
        byIntygsId.stream()
            .filter(a -> a.getStatus() == Status.PENDING_EXTERNAL_ACTION)
            .map(a -> a.getMeddelandeId())
            .collect(Collectors.toList()));
  }

  /**
   * Returnerar ärenden på givet intygsId i status PENDING_INTERNAL_ACTION.
   *
   * <p>Används av ärendeverktyget för att ge förslag på möjliga ärenden att skicka in en påminnelse
   * för.
   */
  @GetMapping("/intyg/{intygsId}/internal")
  public ResponseEntity<List<SimpleArende>> getVantarPaSvarFranOss(
      @PathVariable("intygsId") String intygsId) {
    List<Arende> byIntygsId = arendeRepository.findByIntygsId(intygsId);
    return ResponseEntity.ok(
        byIntygsId.stream()
            .filter(a -> a.getStatus() == Status.PENDING_INTERNAL_ACTION)
            .map(a -> new SimpleArende(a.getMeddelandeId(), a.getRubrik()))
            .collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Arende> getArende(@PathVariable("id") Long id) {
    return ResponseEntity.ok(arendeRepository.findById(id).orElse(null));
  }

  @PostMapping
  public ResponseEntity<Arende> insertQuestion(@RequestBody Arende arende) {
    arende.setTimestamp(LocalDateTime.now());
    arendeRepository.save(arende);
    return ResponseEntity.ok(arende);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuestion(@PathVariable("id") String meddelandeId) {
    Arende arende = arendeRepository.findOneByMeddelandeId(meddelandeId);
    arendeRepository.delete(arende);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping
  public ResponseEntity<ArendeAffectedResponse> deleteAllQuestions() {
    return transactionTemplate.execute(
        new TransactionCallback<ResponseEntity<ArendeAffectedResponse>>() {
          @Override
          public ResponseEntity<ArendeAffectedResponse> doInTransaction(TransactionStatus status) {
            @SuppressWarnings("unchecked")
            List<Arende> arenden =
                entityManager.createQuery("SELECT f FROM Arende f").getResultList();
            for (Arende arende : arenden) {
              entityManager.remove(arende);
            }

            ArendeAffectedResponse affected = new ArendeAffectedResponse(arenden.size());

            return ResponseEntity.ok(affected);
          }
        });
  }

  @DeleteMapping("/enhet/{enhetsId}")
  public ResponseEntity<ArendeAffectedResponse> deleteAllQuestionsOnUnit(
      @PathVariable("enhetsId") String enhetsId) {
    return transactionTemplate.execute(
        new TransactionCallback<ResponseEntity<ArendeAffectedResponse>>() {
          @Override
          public ResponseEntity<ArendeAffectedResponse> doInTransaction(TransactionStatus status) {
            @SuppressWarnings("unchecked")
            List<Arende> arenden =
                entityManager
                    .createQuery("SELECT f FROM Arende f WHERE f.enhetId = :enhetId")
                    .setParameter("enhetId", enhetsId)
                    .getResultList();
            for (Arende arende : arenden) {
              entityManager.remove(arende);
            }

            ArendeAffectedResponse affected = new ArendeAffectedResponse(arenden.size());
            return ResponseEntity.ok(affected);
          }
        });
  }

  @GetMapping("/arendeCount")
  public Long getArendeCountForCertificateIds(@RequestBody List<String> certificateIds) {
    final var arenden = (List<Arende>) arendeRepository.findAll();
    return arenden.stream().filter(arende -> certificateIds.contains(arende.getIntygsId())).count();
  }

  @DeleteMapping("/arende")
  public ResponseEntity<Void> deleteArendenForCertificateIds(
      @RequestBody List<String> certificateIds) {
    final var arenden = (List<Arende>) arendeRepository.findAll();
    final var arendenForDeletion =
        arenden.stream()
            .filter(arende -> certificateIds.contains(arende.getIntygsId()))
            .collect(Collectors.toList());
    arendeRepository.deleteAll(arendenForDeletion);

    return ResponseEntity.ok().build();
  }

  @GetMapping("/arendeDraftCount")
  public Long getArendeDraftCountForCertificateIds(@RequestBody List<String> certificateIds) {
    final var arendeDrafts = (List<ArendeDraft>) arendeDraftRepository.findAll();
    return arendeDrafts.stream()
        .filter(arendeDraft -> certificateIds.contains(arendeDraft.getIntygId()))
        .count();
  }

  @DeleteMapping("/arendeDraft")
  public ResponseEntity<Void> deleteArendeDraftsForCertificateIds(
      @RequestBody List<String> certificateIds) {
    final var arendeDrafts = (List<ArendeDraft>) arendeDraftRepository.findAll();
    final var arendeList =
        arendeDrafts.stream()
            .filter(arendeDraft -> certificateIds.contains(arendeDraft.getIntygId()))
            .collect(Collectors.toList());
    arendeDraftRepository.deleteAll(arendeList);

    return ResponseEntity.ok().build();
  }
}
