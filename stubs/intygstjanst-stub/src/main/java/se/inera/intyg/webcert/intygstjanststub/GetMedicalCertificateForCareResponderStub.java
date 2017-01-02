/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.intygstjanststub;

import java.io.StringReader;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.getmedicalcertificateforcare.v1.*;
import se.inera.intyg.common.fk7263.schemas.clinicalprocess.healthcond.certificate.converter.ModelConverter;
import se.inera.intyg.common.fk7263.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

public class GetMedicalCertificateForCareResponderStub implements
        GetMedicalCertificateForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public GetMedicalCertificateForCareResponseType getMedicalCertificateForCare(String logicalAddress,
            GetMedicalCertificateForCareRequestType request) {

        GetMedicalCertificateForCareResponseType response = new GetMedicalCertificateForCareResponseType();
        CertificateHolder intygResponse = intygStore.getIntygForCertificateId(request.getCertificateId());
        if (intygResponse != null) {
            response.setMeta(ModelConverter.toCertificateMetaType(intygResponse));
            attachCertificateDocument(intygResponse, response);
            if (intygResponse.isRevoked()) {
                response.setResult(
                        ResultTypeUtil.errorResult(ErrorIdType.REVOKED, String.format("Certificate '%s' has been revoked", intygResponse.getId())));
            } else {
                response.setResult(ResultTypeUtil.okResult());
            }
        } else {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, "Intyg not found in stub"));
        }
        return response;
    }

    private void attachCertificateDocument(CertificateHolder certificate, GetMedicalCertificateForCareResponseType response) {
        String content = intygStore.getContentTemplate("intyg-fk7263-content.xml")
                .replace("CERTIFICATE_ID", certificate.getId())
                .replace("PATIENT_CRN", certificate.getCivicRegistrationNumber().getPersonnummer())
                .replace("CAREUNIT_ID", certificate.getCareUnitId())
                .replace("CAREUNIT_NAME", certificate.getCareUnitName())
                .replace("CAREGIVER_ID", certificate.getCareGiverId())
                .replace("DOCTOR_NAME", certificate.getSigningDoctorName())
                .replace("SIGNED_DATE", certificate.getSignedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        RegisterMedicalCertificateType jaxbObject = JAXB.unmarshal(new StringReader(content),
                RegisterMedicalCertificateType.class);
        response.setLakarutlatande(jaxbObject.getLakarutlatande());
    }
}
