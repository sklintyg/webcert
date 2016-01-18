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

package se.inera.intyg.webcert.common.client;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.webcert.common.client.converter.RevokeRequestConverter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;

/**
 * Exposes the RevokeMedicalCertificate SOAP service.
 *
 * Created by eriklupander on 2015-06-03.
 */
@Component
public class RevokeCertificateServiceClientImpl implements RevokeCertificateServiceClient {

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeService;

    @Autowired
    private RevokeRequestConverter revokeRequestConverter;

    @Override
    public RevokeMedicalCertificateResponseType revokeCertificate(String xml, String logicalAddress) {

        validateArgument(xml, "xml missing, cannot invoke revokeMedicalCertificate service");
        validateArgument(logicalAddress, "Logical address missing, cannot invoke revokeMedicalCertificate service");

        try {
            RevokeMedicalCertificateRequestType request = revokeRequestConverter.fromXml(xml);

            AttributedURIType uri = new AttributedURIType();
            uri.setValue(logicalAddress);

            return revokeService.revokeMedicalCertificate(uri, request);
        } catch (JAXBException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private void validateArgument(String arg, String msg) {
        if (arg == null || arg.trim().length() == 0) {
            throw new IllegalArgumentException(msg);
        }
    }
}
