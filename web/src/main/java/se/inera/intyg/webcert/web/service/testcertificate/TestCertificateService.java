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
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.TestCertificateEraseResult;

@Service
public class TestCertificateService {

    private static final Logger LOG = LoggerFactory.getLogger(TestCertificateService.class);

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private EraseTestCertificateService eraseTestCertificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Transactional (readOnly = true)
    public TestCertificateEraseResult eraseTestCertificates(LocalDateTime from, LocalDateTime to) {
        final Set<String> erasedTestCertificates = new HashSet<>();
        final Set<String> failedTestCertificates = new HashSet<>();

        final List<String> certificateIds = getTestCertificateToErase(from, to);

        for (String certificateId: certificateIds) {
            if (skipIfAlreadyErasedDueToRelation(certificateId, erasedTestCertificates)) {
                continue;
            }

            final List<String> idsToErase = new ArrayList<>();
            idsToErase.add(certificateId);
            collectCertificateIdsToErase(certificateId, idsToErase);

            final Map<String, String> unitMap = new Hashtable<>(idsToErase.size());
            final Map<String, String> userMap = new Hashtable<>(idsToErase.size());
            collectLogInformation(idsToErase, unitMap, userMap);

            final List<String> idsToLog = new ArrayList<>(idsToErase.size());

            try {
                eraseTestCertificateService.eraseTestCertificates(idsToErase);
                erasedTestCertificates.addAll(idsToErase);
                idsToLog.addAll(idsToErase);
            } catch (Exception ex) {
                LOG.error(
                    String.format("Couldn't not erase certificate with ids %s when erasing test certificates", idsToErase.toString()), ex);
                failedTestCertificates.addAll(idsToErase);
            }

            logErasedCertificates(idsToLog, unitMap, userMap);
        }

        return TestCertificateEraseResult.create(erasedTestCertificates.size(), failedTestCertificates.size());
    }

    private boolean skipIfAlreadyErasedDueToRelation(String certificateId, Set<String> erasedTestCertificates) {
        return erasedTestCertificates.contains(certificateId);
    }

    private void collectLogInformation(List<String> idsToErase, Map<String, String> unitMap, Map<String, String> userMap) {
        for (String idToErase: idsToErase) {
            final Utkast certificateToErase = utkastRepository.getOne(idToErase);
            unitMap.put(certificateToErase.getIntygsId(), certificateToErase.getEnhetsId());
            userMap.put(certificateToErase.getIntygsId(), certificateToErase.getSkapadAv().getHsaId());
        }
    }

    private void logErasedCertificates(List<String> idsToLog, Map<String, String> unitMap, Map<String, String> userMap) {
        for (String idToLog: idsToLog) {
            monitoringLogService.logTestCertificateErased(idToLog, unitMap.get(idToLog), userMap.get(idToLog));
        }
    }

    private void collectCertificateIdsToErase(String certificateId, List<String> idsToErase) {
        final List<WebcertCertificateRelation> parentRelations = utkastRepository.findParentRelation(certificateId);
        for (WebcertCertificateRelation parentRelation: parentRelations) {
            final String parentRelationId = parentRelation.getIntygsId();
            if (!idsToErase.contains(parentRelationId)) {
                idsToErase.add(parentRelationId);
                collectCertificateIdsToErase(parentRelationId, idsToErase);
            }
        }

        final List<WebcertCertificateRelation> childrenRelations = utkastRepository.findChildRelations(certificateId);
        for (WebcertCertificateRelation childRelation: childrenRelations) {
            final String childRelationId = childRelation.getIntygsId();
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
            return  utkastRepository.findTestCertificatesByCreatedAfter(from);
        } else {
            return utkastRepository.findTestCertificates();
        }
    }
}
