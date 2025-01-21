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
package se.inera.intyg.webcert.web.service.testcertificate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.infra.testcertificate.dto.TestCertificateEraseResult;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
public class TestCertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(TestCertificateService.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private EraseTestCertificateService eraseTestCertificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Transactional(readOnly = true)
    public TestCertificateEraseResult eraseTestCertificates(LocalDateTime from, LocalDateTime to) {
        final var erasedTestCertificateIds = new HashSet<String>();
        final var failedTestCertificateIds = new HashSet<String>();

        final var certificateIds = getTestCertificateToErase(from, to);

        for (var certificateId : certificateIds) {
            if (skipIfAlreadyErasedDueToRelation(certificateId, erasedTestCertificateIds)) {
                continue;
            }

            final var idsToErase = new ArrayList<String>();
            idsToErase.add(certificateId);
            collectCertificateIdsToErase(certificateId, idsToErase);

            final var unitMap = new Hashtable<String, String>(idsToErase.size());
            final var userMap = new Hashtable<String, String>(idsToErase.size());
            collectLogInformation(idsToErase, unitMap, userMap);

            final var idsToLog = new ArrayList<String>(idsToErase.size());

            try {
                eraseTestCertificateService.eraseTestCertificates(idsToErase);
                erasedTestCertificateIds.addAll(idsToErase);
                idsToLog.addAll(idsToErase);
            } catch (Exception ex) {
                LOG.error(
                    String.format("Couldn't not erase certificate with ids %s when erasing test certificates", idsToErase.toString()), ex);
                failedTestCertificateIds.addAll(idsToErase);
            }

            logErasedCertificates(idsToLog, unitMap, userMap);
        }

        return TestCertificateEraseResult.create(erasedTestCertificateIds.size(), failedTestCertificateIds.size());
    }

    private boolean skipIfAlreadyErasedDueToRelation(String certificateId, Set<String> erasedTestCertificates) {
        return erasedTestCertificates.contains(certificateId);
    }

    private void collectLogInformation(List<String> idsToErase, Map<String, String> unitMap, Map<String, String> userMap) {
        for (var idToErase : idsToErase) {
            final var certificateToErase = utkastRepository.getOne(idToErase);
            unitMap.put(certificateToErase.getIntygsId(), certificateToErase.getEnhetsId());
            userMap.put(certificateToErase.getIntygsId(), certificateToErase.getSkapadAv().getHsaId());
        }
    }

    private void logErasedCertificates(List<String> idsToLog, Map<String, String> unitMap, Map<String, String> userMap) {
        for (var idToLog : idsToLog) {
            monitoringLogService.logTestCertificateErased(idToLog, unitMap.get(idToLog), userMap.get(idToLog));
        }
    }

    private void collectCertificateIdsToErase(String certificateId, List<String> idsToErase) {
        final var parentRelations = utkastRepository.findParentRelation(certificateId);
        for (var parentRelation : parentRelations) {
            final var parentRelationId = parentRelation.getIntygsId();
            if (!idsToErase.contains(parentRelationId)) {
                idsToErase.add(parentRelationId);
                collectCertificateIdsToErase(parentRelationId, idsToErase);
            }
        }

        final var childrenRelations = utkastRepository.findChildRelations(certificateId);
        for (var childRelation : childrenRelations) {
            final var childRelationId = childRelation.getIntygsId();
            if (!idsToErase.contains(childRelationId)) {
                idsToErase.add(childRelationId);
                collectCertificateIdsToErase(childRelationId, idsToErase);
            }
        }
    }

    private List<String> getTestCertificateToErase(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return utkastRepository.findTestCertificatesByCreatedBeforeAndAfter(from, to);
        } else if (to != null) {
            return utkastRepository.findTestCertificatesByCreatedBefore(to);
        } else if (from != null) {
            return utkastRepository.findTestCertificatesByCreatedAfter(from);
        } else {
            return utkastRepository.findTestCertificates();
        }
    }
}
