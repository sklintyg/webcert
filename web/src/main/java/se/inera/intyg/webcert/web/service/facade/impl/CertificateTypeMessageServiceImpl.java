/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateTypeMessageService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@Service
public class CertificateTypeMessageServiceImpl implements CertificateTypeMessageService {

    private final UtkastService utkastService;
    private final WebCertUserService webCertUserService;
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    public CertificateTypeMessageServiceImpl(UtkastService utkastService,
        WebCertUserService webCertUserService, AuthoritiesHelper authoritiesHelper) {
        this.utkastService = utkastService;
        this.webCertUserService = webCertUserService;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public Optional<String> get(String certificateType, Personnummer patientId) {
        if (!authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, certificateType)) {
            return Optional.empty();
        }
        final var user = webCertUserService.getUser();
        final Map<String, Map<String, PreviousIntyg>> previousCertificates = utkastService
            .checkIfPersonHasExistingIntyg(patientId, user, null);

        final var previousCertificateMap = previousCertificates.getOrDefault(INTYG_INDICATOR, Collections.emptyMap());
        if (previousCertificateMap.containsKey(certificateType)) {
            final var previousIntyg = previousCertificateMap.get(certificateType);
            if (previousIntyg.isSameVardgivare() && previousIntyg.isSameEnhet()) {
                return Optional.of(
                    "Det finns ett signerat dödsbevis för detta personnummer."
                        + " Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset.");
            }
            if (previousIntyg.isSameVardgivare() && !previousIntyg.isSameEnhet()) {
                return Optional.of(
                    "Det finns ett signerat dödsbevis för detta personnummer på annan vårdenhet."
                        + " Du kan inte skapa ett nytt dödsbevis men kan däremot välja att ersätta det befintliga dödsbeviset.");
            }
            if (!previousIntyg.isSameVardgivare()) {
                return Optional.of(
                    "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare."
                        + " Det är inte möjligt att skapa ett nytt dödsbevis.");
            }
        }

        final var previousDraftMap = previousCertificates.getOrDefault(UTKAST_INDICATOR, Collections.emptyMap());
        if (previousDraftMap.containsKey(certificateType)) {
            final var previousIntyg = previousDraftMap.get(certificateType);
            if (previousIntyg.isSameVardgivare() && previousIntyg.isSameEnhet()) {
                return Optional.of(
                    "Det finns ett utkast på dödsbevis för detta personnummer."
                        + " Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.");
            }
            if (previousIntyg.isSameVardgivare() && !previousIntyg.isSameEnhet()) {
                return Optional.of(
                    "Det finns ett utkast på dödsbevis för detta personnummer på annan vårdenhet."
                        + " Du kan inte skapa ett nytt utkast men kan däremot välja att fortsätta med det befintliga utkastet.");
            }
            if (!previousIntyg.isSameVardgivare()) {
                return Optional.of(
                    "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare."
                        + " Senast skapade dödsbevis är det som gäller. "
                        + "Om du fortsätter och lämnar in dödsbeviset så blir det därför detta dödsbevis som gäller.");
            }
        }

        return Optional.empty();
    }
}
