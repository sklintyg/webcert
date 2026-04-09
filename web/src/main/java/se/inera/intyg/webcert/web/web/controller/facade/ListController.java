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
package se.inera.intyg.webcert.web.web.controller.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListPreviousCertificatesFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListQuestionsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListSignedCertificatesFacadeServiceImpl;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ListResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.list.ListRequestDTO;

@RestController
@RequestMapping("/api/list")
public class ListController {

  private static final Logger LOG = LoggerFactory.getLogger(ListController.class);

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  private final ListDraftsFacadeServiceImpl listDraftsFacadeService;
  private final ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService;
  private final ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService;
  private final ListQuestionsFacadeServiceImpl listQuestionsFacadeService;

  @Autowired
  public ListController(
      ListDraftsFacadeServiceImpl listDraftsFacadeService,
      ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService,
      ListPreviousCertificatesFacadeServiceImpl listPreviousCertificatesFacadeService,
      ListQuestionsFacadeServiceImpl listQuestionsFacadeService) {
    this.listDraftsFacadeService = listDraftsFacadeService;
    this.listSignedCertificatesFacadeService = listSignedCertificatesFacadeService;
    this.listPreviousCertificatesFacadeService = listPreviousCertificatesFacadeService;
    this.listQuestionsFacadeService = listQuestionsFacadeService;
  }

  @PostMapping("/draft")
  @PerformanceLogging(
      eventAction = "list-get-list-of-drafts",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ListResponseDTO> getListOfDrafts(@RequestBody ListRequestDTO request) {
    LOG.debug("Getting list of drafts");
    final var listInfo = listDraftsFacadeService.get(request.getFilter());
    return ResponseEntity.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount()));
  }

  @PostMapping("/certificate")
  @PerformanceLogging(
      eventAction = "list-get-list-of-signed-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ListResponseDTO> getListOfSignedCertificates(
      @RequestBody ListRequestDTO request) {
    final var listInfo = listSignedCertificatesFacadeService.get(request.getFilter());
    return ResponseEntity.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount()));
  }

  @PostMapping("/previous")
  @PerformanceLogging(
      eventAction = "list-get-list-of-previous-certificates",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ListResponseDTO> getListOfPreviousCertificates(
      @RequestBody ListRequestDTO request) {
    final var listInfo = listPreviousCertificatesFacadeService.get(request.getFilter());
    return ResponseEntity.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount()));
  }

  @PostMapping("/question")
  @PerformanceLogging(
      eventAction = "list-get-list-of-certificates-with-questions",
      eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
  public ResponseEntity<ListResponseDTO> getListOfCertificatesWithQuestions(
      @RequestBody ListRequestDTO request) {
    final var listInfo = listQuestionsFacadeService.get(request.getFilter());
    return ResponseEntity.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount()));
  }
}
