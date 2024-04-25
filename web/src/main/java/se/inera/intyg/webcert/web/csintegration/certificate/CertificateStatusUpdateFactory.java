/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PQType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PartialDateType;

public class CertificateStatusUpdateFactory {

    private CertificateStatusUpdateFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] create(Certificate certificate, String encodedXmlRepresentation, HandelsekodEnum eventType,
        LocalDateTime now) {
        final var request = getRegisterCertificateType(encodedXmlRepresentation);
        
        final var certificateStatusUpdateForCareType = new CertificateStatusUpdateForCareType();
        certificateStatusUpdateForCareType.setIntyg(request.getIntyg());
        certificateStatusUpdateForCareType.setHandelse(
            NotificationRedeliveryUtil.getEventV3(eventType, now, null, null)
        );
        certificateStatusUpdateForCareType.setSkickadeFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        certificateStatusUpdateForCareType.setMottagnaFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        certificateStatusUpdateForCareType.setHanteratAv(
            NotificationRedeliveryUtil.getIIType(new HsaId(), certificate.getMetadata().getIssuedBy().getPersonId(), HSA_ID_OID)
        );

        final var factory = new ObjectFactory();
        final var certificateStatusUpdateForCare = factory.createCertificateStatusUpdateForCare(certificateStatusUpdateForCareType);
        return convertToByteArray(certificateStatusUpdateForCare);
    }

    private static RegisterCertificateType getRegisterCertificateType(String encodedXmlRepresentation) {
        final var decodedXmlRepresentation = new String(
            Base64.getDecoder().decode(encodedXmlRepresentation),
            StandardCharsets.UTF_8
        );
        final var element = XmlMarshallerHelper.unmarshal(decodedXmlRepresentation);
        return (RegisterCertificateType) element.getValue();
    }

    private static byte[] convertToByteArray(JAXBElement<CertificateStatusUpdateForCareType> jaxbElement) {
        try {
            final var context = JAXBContext.newInstance(
                CertificateStatusUpdateForCareType.class,
                DatePeriodType.class,
                PartialDateType.class,
                XPathType.class,
                PQType.class
            );

            final var marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final var outputStream = new ByteArrayOutputStream();
            marshaller.marshal(jaxbElement, outputStream);

            return outputStream.toByteArray();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }
}
