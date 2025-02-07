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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.common.support.facade.model.CertificateRelationType.COMPLEMENTED;

import java.util.Optional;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.common.luae_na.support.LuaenaEntryPoint;
import se.inera.intyg.common.luse.support.LuseEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.ts_bas.support.TsBasEntryPoint;
import se.inera.intyg.common.ts_diabetes.support.TsDiabetesEntryPoint;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CertificateSignAndSendFunctionImpl implements CertificateSignAndSendFunction {

    private final AuthoritiesHelper authoritiesHelper;
    private static final String SIGN_AND_SEND_NAME = "Signera och skicka";
    private static final String SIGN_NAME = "Signera intyget";
    private static final String SIGN_DESCRIPTION = "Intyget signeras.";
    private static final String SIGN_AND_SEND_DESCRIPTION_ARBETSFORMEDLINGEN = "Intyget skickas direkt till Arbetsförmedlingen.";
    private static final String SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN = "Intyget skickas direkt till Försäkringskassan.";
    private static final String SIGN_AND_SEND_DESCRIPTION_SKATTEVERKET = "Intyget skickas direkt till Skatteverket.";
    private static final String SIGN_AND_SEND_DESCRIPTION_SOCIALSTYRELSEN = "Intyget skickas direkt till Socialstyrelsen.";
    private static final String SIGN_AND_SEND_DESCRIPTION_TRANSPORTSTYRELSEN = "Intyget skickas direkt till Transportstyrelsen.";


    public CertificateSignAndSendFunctionImpl(AuthoritiesHelper authoritiesHelper) {
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate) {
        if (isSignedAndSendDirectly(certificate)) {
            return Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE,
                SIGN_AND_SEND_NAME,
                sendToDescription(certificate.getMetadata().getType()),
                true
            ));
        }
        return Optional.of(ResourceLinkDTO.create(
            ResourceLinkTypeDTO.SIGN_CERTIFICATE,
            SIGN_NAME,
            SIGN_DESCRIPTION,
            true
        ));
    }

    private String sendToDescription(String certificateType) {
        if (shouldBeSentToSkatteverket(certificateType)) {
            return SIGN_AND_SEND_DESCRIPTION_SKATTEVERKET;
        }

        if (shouldBeSentToSocialstyrelsen(certificateType)) {
            return SIGN_AND_SEND_DESCRIPTION_SOCIALSTYRELSEN;
        }

        if (shouldBeSentToArbetsformedlingen(certificateType)) {
            return SIGN_AND_SEND_DESCRIPTION_ARBETSFORMEDLINGEN;
        }

        if (shouldBeSentToForsakringskassan(certificateType)) {
            return SIGN_AND_SEND_DESCRIPTION_FORSAKRINGSKASSAN;
        }

        if (shouldBeSentToTransportstyrelsen(certificateType)) {
            return SIGN_AND_SEND_DESCRIPTION_TRANSPORTSTYRELSEN;
        }

        return null;
    }

    private boolean shouldBeSentToSkatteverket(String certificateType) {
        return DbModuleEntryPoint.MODULE_ID.equals(certificateType);
    }

    private boolean shouldBeSentToSocialstyrelsen(String certificateType) {
        return DoiModuleEntryPoint.MODULE_ID.equals(certificateType);
    }

    private boolean shouldBeSentToArbetsformedlingen(String certificateType) {
        return Af00213EntryPoint.MODULE_ID.equals(certificateType);
    }

    private boolean shouldBeSentToForsakringskassan(String certificateType) {
        return Fk7263EntryPoint.MODULE_ID.equals(certificateType) || LuseEntryPoint.MODULE_ID.equals(certificateType)
            || LisjpEntryPoint.MODULE_ID.equals(certificateType) || LuaefsEntryPoint.MODULE_ID.equals(certificateType)
            || LuaenaEntryPoint.MODULE_ID.equals(certificateType);
    }

    private boolean shouldBeSentToTransportstyrelsen(String certificateType) {
        return TsBasEntryPoint.MODULE_ID.equals(certificateType) || TsDiabetesEntryPoint.MODULE_ID.equals(certificateType);
    }

    private boolean isSignedAndSendDirectly(Certificate certificate) {
        return (authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, certificate.getMetadata().getType())
            || isComplementingCertificate(certificate)) && !certificate.getMetadata().getPatient().isTestIndicated();
    }

    private boolean isComplementingCertificate(Certificate certificate) {
        return certificate.getMetadata().getRelations() != null && certificate.getMetadata().getRelations().getParent() != null
            && certificate.getMetadata().getRelations().getParent().getType() == COMPLEMENTED;
    }
}
