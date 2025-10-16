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
package se.inera.intyg.webcert.web.service.certificate;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.web.integration.ITIntegrationService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@Service
public class CertificateServiceImpl implements CertificateService {

    private ITIntegrationService itIntegrationService;
    private PatientDetailsResolver patientDetailsResolver;
    private LogService logService;
    private WebCertUserService webcertUserService;
    private AuthoritiesHelper authoritiesHelper;

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    public CertificateServiceImpl(ITIntegrationService itIntegrationService, PatientDetailsResolver patientDetailsResolver,
        WebCertUserService webCertUserService, LogService logService, AuthoritiesHelper authoritiesHelper) {
        this.itIntegrationService = itIntegrationService;
        this.patientDetailsResolver = patientDetailsResolver;
        this.webcertUserService = webCertUserService;
        this.logService = logService;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public CertificateListResponse listCertificatesForDoctor(QueryIntygParameter queryParam) {
        try {
            final WebCertUser user = webcertUserService.getUser();
            Set<String> types = getCertificateTypes();
            final var responseFromIT = itIntegrationService.getCertificatesForDoctor(queryParam, types);

            responseFromIT.setCertificates(responseFromIT.getCertificates().stream()
                .filter(Objects::nonNull)
                .map(this::decoratePatientWithFlags)
                .collect(Collectors.toList())
            );

            responseFromIT.getCertificates().stream().map(CertificateListEntry::getCivicRegistrationNumber).distinct()
                .forEach(patientId -> {
                    logService.logReadLevelTwo(user, getCivicRegistrationNumber(patientId).get().getPersonnummerWithDash());
                });
            return responseFromIT;
        } catch (Exception ex) {
            LOGGER.error("Could not get list of signed certificates for unit from IT", ex);
            CertificateListResponse certificateListResponse = new CertificateListResponse();
            certificateListResponse.setErrorFromIT(true);
            certificateListResponse.setCertificates(Collections.emptyList());
            return certificateListResponse;
        }
    }


    private CertificateListEntry decoratePatientWithFlags(CertificateListEntry certificate) {
        Optional<Personnummer> civicRegistrationNumber = getCivicRegistrationNumber(certificate.getCivicRegistrationNumber());
        if (civicRegistrationNumber.isPresent()) {
            SekretessStatus protectedIdentityStatus = patientDetailsResolver.getSekretessStatus(civicRegistrationNumber.get());
            boolean hasProtectedIdentity = protectedIdentityStatus == SekretessStatus.TRUE
                || protectedIdentityStatus == SekretessStatus.UNDEFINED;
            certificate.setProtectedIdentity(hasProtectedIdentity);
            certificate.setDeceased(patientDetailsResolver.isAvliden(civicRegistrationNumber.get()));
            certificate.setTestIndicator(patientDetailsResolver.isTestIndicator(civicRegistrationNumber.get()));
        }
        return certificate;
    }

    private Set<String> getCertificateTypes() {
        return authoritiesHelper.getIntygstyperForPrivilege(webcertUserService.getUser(), AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
    }

    private Optional<Personnummer> getCivicRegistrationNumber(String civicRegistrationNumber) {
        return Personnummer.createPersonnummer(civicRegistrationNumber);
    }
}

