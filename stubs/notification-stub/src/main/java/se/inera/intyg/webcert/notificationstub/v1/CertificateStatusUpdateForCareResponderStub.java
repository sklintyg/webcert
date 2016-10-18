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

package se.inera.intyg.webcert.notificationstub.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.intygstyper.fk7263.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.webcert.notificationstub.NotificationStore;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.*;


public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    @Autowired
    private NotificationStore notificationStore;

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {

        UtlatandeType utlatande = request.getUtlatande();

        String handelseKod = utlatande.getHandelse().getHandelsekod().getCode();
        String utlatandeId = utlatande.getUtlatandeId().getExtension();

        StringBuilder sb = new StringBuilder();

        if (utlatande.getSigneringsdatum() != null) {
            sb.append(" Signeringsdatum: " + utlatande.getSigneringsdatum());
            sb.append("\n");
        }

        if (utlatande.getPatient() != null) {
            sb.append(" Patient: " + utlatande.getPatient().getPersonId().getExtension());
            sb.append("\n");
        }

        if (utlatande.getDiagnos() != null) {
            sb.append(" Diagnoskod: " + utlatande.getDiagnos().getCode());
            sb.append("\n");
        }

        if (!utlatande.getArbetsformaga().isEmpty()) {
            sb.append(" Arbetsformagor: ");
            for (Arbetsformaga arbFormaga : utlatande.getArbetsformaga()) {
                sb.append("[" + arbFormaga.getVarde().getValue() + "% ");
                sb.append(arbFormaga.getPeriod().getFrom() + "->");
                sb.append(arbFormaga.getPeriod().getTom() + "] ");
            }
            sb.append("\n");
        }

        FragorOchSvar fs = utlatande.getFragorOchSvar();
        sb.append(" Fragor: " + fs.getAntalFragor());
        sb.append(", Hant. fragor: " + fs.getAntalHanteradeFragor());
        sb.append(", Svar: " + fs.getAntalSvar());
        sb.append(", Hant. svar: " + fs.getAntalHanteradeSvar());
        sb.append("\n");

        LOG.info("\n*********************************************************************************\n"
                + " Request to address '{}' recieved for intyg: {} handelse: {}.\n"
                + "{}"
                + "*********************************************************************************", logicalAddress, utlatandeId, handelseKod, sb.toString());

        notificationStore.put(utlatandeId, request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");

        return response;
    }

}
