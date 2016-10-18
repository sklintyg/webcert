/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.intygstyper.fkparent.support.ResultTypeUtil;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.intygstyper.fkparent.model.converter.CertificateStateHolderConverter;
import se.inera.intyg.webcert.intygstjanststub.mode.StubLatencyAware;
import se.inera.intyg.webcert.intygstjanststub.mode.StubModeAware;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v2.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;

public class ListCertificatesForCareResponderStub implements ListCertificatesForCareResponderInterface {

    @Autowired
    private IntygStore intygStore;

    @Override
    @StubLatencyAware
    @StubModeAware
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {

        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();
        response.setIntygsLista(new ListaType());
        response.getIntygsLista().getIntyg();

        List<CertificateHolder> intygForEnhetAndPersonnummer = intygStore.getIntygForEnhetAndPersonnummer(
                parameters.getEnhetsId().stream().map(HsaId::getExtension).collect(Collectors.toList()), parameters.getPersonId().getExtension());

        for (CertificateHolder cert : intygForEnhetAndPersonnummer) {
            Intyg intyg = getIntyg(cert);
            intyg.getStatus().addAll(CertificateStateHolderConverter.toIntygsStatusType(cert.getCertificateStates()));
            response.getIntygsLista().getIntyg().add(intyg);
        }

        response.setResult(ResultTypeUtil.okResult());
        return response;
    }

    private Intyg getIntyg(CertificateHolder certificate) {
        String content = intygStore.getContentTemplate("minimal-rivta-content.xml")
                .replace("CERTIFICATE_ID", certificate.getId())
                .replace("CERTIFICATE_TYPE", certificate.getType().toUpperCase())
                .replace("PATIENT_CRN", certificate.getCivicRegistrationNumber().getPersonnummerWithoutDash())
                .replace("CAREUNIT_ID", certificate.getCareUnitId())
                .replace("CAREUNIT_NAME", certificate.getCareUnitName())
                .replace("CAREGIVER_ID", certificate.getCareGiverId())
                .replace("DOCTOR_NAME", certificate.getSigningDoctorName())
                .replace("SIGNED_DATE", certificate.getSignedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        RegisterCertificateType jaxbObject = JAXB.unmarshal(new StringReader(content),
                RegisterCertificateType.class);
        return jaxbObject.getIntyg();
    }
}
