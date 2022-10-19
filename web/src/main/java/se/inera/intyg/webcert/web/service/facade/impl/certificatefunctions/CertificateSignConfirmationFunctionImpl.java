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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CertificateSignConfirmationFunctionImpl implements CertificateSignConfirmationFunction {

    private final UtkastService utkastService;
    private final AuthoritiesHelper authoritiesHelper;

    @Autowired
    public CertificateSignConfirmationFunctionImpl(UtkastService utkastService,
        AuthoritiesHelper authoritiesHelper) {
        this.utkastService = utkastService;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate, WebCertUser webCertUser) {
        if (!authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, certificate.getMetadata().getType())) {
            return Optional.empty();
        }
        final var personnummer = Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).get();
        final var existingIntyg = utkastService.checkIfPersonHasExistingIntyg(personnummer, webCertUser, certificate.getMetadata().getId());
        final var previousIntygMap = existingIntyg.getOrDefault(INTYG_INDICATOR, Collections.emptyMap());
        if (previousIntygMap.containsKey(certificate.getMetadata().getType())) {
            return Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
                "Signera och skicka",
                "Intyget skickas direkt till Skatteverket",
                "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare. Det är därför inte möjligt att signera detta dödsbevis.",
                true));
        }
        final var previousUtkastMap = existingIntyg.getOrDefault(UTKAST_INDICATOR, Collections.emptyMap());
        if (previousUtkastMap.containsKey(certificate.getMetadata().getType())) {
            return Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
                "Signera och skicka",
                "Intyget skickas direkt till Skatteverket",
                "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare. Senast skapade dödsbevis är det som gäller. Om du fortsätter och lämnar in dödsbeviset så blir det därför detta dödsbevis som gäller.",
                true));
        }
        return Optional.empty();
    }
}
