/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notificationstub.v3;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

@SchemaValidation
public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);

    @Autowired
    private NotificationStoreV3 notificationStoreV3;

    @Autowired
    private NotificationStubStateBean notificationStubStateBean;

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
        CertificateStatusUpdateForCareType request) {

        Intyg intyg = request.getIntyg();

        String handelseKod = request.getHandelse().getHandelsekod().getCode();
        String intygsId = intyg.getIntygsId().getExtension();

        StringBuilder sb = new StringBuilder();

        if (request.getHanteratAv() != null) {
            sb.append("Hanterat av: " + request.getHanteratAv().getExtension());
            sb.append("\n");
        }

        if (intyg.getSigneringstidpunkt() != null) {
            sb.append(" Signeringstidpunkt: " + intyg.getSigneringstidpunkt());
            sb.append("\n");
        }

        if (intyg.getPatient() != null) {
            sb.append(" Patient: " + intyg.getPatient().getPersonId().getExtension());
            sb.append("\n");
        }

        Arenden skickadeFragor = request.getSkickadeFragor();
        sb.append(" Skickade fragor totalt: " + skickadeFragor.getTotalt());
        sb.append(" Skickade fragor hanterade: " + skickadeFragor.getHanterade());
        sb.append(" Skickade fragor besvarade: " + skickadeFragor.getBesvarade());
        sb.append(" Skickade fragor ej besvarade: " + skickadeFragor.getEjBesvarade());
        sb.append("\n");
        Arenden mottagnaFragor = request.getMottagnaFragor();
        sb.append(" Mottagna fragor totalt: " + mottagnaFragor.getTotalt());
        sb.append(" Mottagna fragor hanterade: " + mottagnaFragor.getHanterade());
        sb.append(" Mottagna fragor besvarade: " + mottagnaFragor.getBesvarade());
        sb.append(" Mottagna fragor ej besvarade: " + mottagnaFragor.getEjBesvarade());
        sb.append("\n");

        LOG.info("\n*********************************************************************************\n"
                + " Request to address '{}' received for intyg: {} handelse: {}.\n"
                + "{}"
                + "*********************************************************************************", logicalAddress, intygsId,
            handelseKod, sb.toString());

        notificationStoreV3.put(request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(ResultTypeUtil.okResult());
        LOG.debug("Request set to 'OK'");

        performErrorEmulation(notificationStubStateBean.getErrorCode(), request, response);
        return response;
    }

    // There are still some duplicate code in the mock used for for testing in notification-sender:
    // se.inera.intyg.webcert.notification_sender.mocks.v3.CertificateStatusUpdateForCareResponderStub.performErrorEmulation
    private void performErrorEmulation(String errorCode, CertificateStatusUpdateForCareType request,
        CertificateStatusUpdateForCareResponseType response) {
        if (errorCode == null) {
            return;
        }

        LOG.debug("emulateError: " + errorCode);
        switch (errorCode) {
            case "1":
                if (request.getHandelse().getHandelsekod().getCode().matches("^ANDRAT$")) {
                    LOG.debug("Stub messing upp response. Fel B. Only for ANDRAT notifications.");
                    response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, "Certificate not found "
                        + "in COSMIC and ref field is missing, cannot store certificate. "
                        + "Possible race condition. Retry later when the certificate may have been stored in COSMIC. "
                        + "| Log Id: 01182b7d-9d19-4d5a-b892-18342670668c"));
                }
                break;
            case "2":
                LOG.debug("Stub messing upp response. TechError null.");
                response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, null));
                break;
            case "3":
                LOG.debug("Stub messing upp response. TechError Unspecified Service.");
                response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, "Unspecified service error"));
                break;
            case "4":
                throw new RuntimeException("This is an emulated error from the stub, should result in a 500 Server Error");
            case "5":
                LOG.debug("Stub messing upp response. Fel B. For all notifications.");
                response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR, "Certificate not found "
                    + "in COSMIC and ref field is missing, cannot store certificate. "
                    + "Possible race condition. Retry later when the certificate may have been stored in COSMIC. "
                    + "| Log Id: 01182b7d-9d19-4d5a-b892-18342670668c"));
                break;
            case "6":
                LOG.debug("Stub messing upp response. APPLICATION_ERROR");
                response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Something went terribly wrong!"));
                break;
            case "7":
                LOG.debug("Stub messing upp response. VALIDATION_ERROR");
                response.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, "This does not validate!"));
                break;
            case "8":
                LOG.debug("Stub messing upp response. Setting as INFO");
                response.setResult(ResultTypeUtil.infoResult("Thank you!"));
                break;
            default:
                LOG.debug("Stub OK. No error emulated.");
                break;
        }
    }
}
