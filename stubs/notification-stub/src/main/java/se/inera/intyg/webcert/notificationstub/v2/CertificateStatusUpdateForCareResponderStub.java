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

package se.inera.intyg.webcert.notificationstub.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.webcert.notificationstub.NotificationStore;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.FragorOchSvar;
import se.riv.clinicalprocess.healthcond.certificate.v2.Intyg;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    @Autowired
    private NotificationStore notificationStore;

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {

        Intyg intyg = request.getIntyg();

        String handelseKod = request.getHandelse().getHandelsekod().getCode();
        String intygsId = intyg.getIntygsId().getExtension();

        StringBuilder sb = new StringBuilder();

        if (intyg.getSigneringstidpunkt() != null) {
            sb.append(" Signeringstidpunkt: " + intyg.getSigneringstidpunkt());
            sb.append("\n");
        }

        if (intyg.getPatient() != null) {
            sb.append(" Patient: " + intyg.getPatient().getPersonId().getExtension());
            sb.append("\n");
        }

        FragorOchSvar fs = request.getFragorOchSvar();
        sb.append(" Fragor: " + fs.getAntalFragor());
        sb.append(", Hant. fragor: " + fs.getAntalHanteradeFragor());
        sb.append(", Svar: " + fs.getAntalSvar());
        sb.append(", Hant. svar: " + fs.getAntalHanteradeSvar());
        sb.append("\n");

        LOG.info("\n*********************************************************************************\n"
                + " Request to address '{}' recieved for intyg: {} handelse: {}.\n"
                + "{}"
                + "*********************************************************************************", logicalAddress, intygsId, handelseKod, sb.toString());

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");

        return response;
    }

}
