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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static se.inera.intyg.common.support.Constants.KV_HANDELSE_CODE_SYSTEM;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.xml.bind.JAXBContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.DatePeriodType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

class NotificationMessageMapperTest {

    private static final String ID = "id1";
    private static final String TYPE_1 = "type1";
    private static final String UNIT_ID = "unit1";
    private static final String EXTERNAL_REF = "externalRef";
    private static final String PERSON_ID = "personId";
    private static final Intyg EXPECTED_INTYG = new Intyg();
    private static final String CERTIFICATE_ID = "certificateId";
    private Certificate certificate;
    private String xmlRepresentation;
    private HandelsekodEnum eventType;
    private NotificationMessageMapper converter;
    private RegisterCertificateType registerCertificateType;

    @BeforeEach
    void setUp() {
        CertificateMetadata metadata = CertificateMetadata.builder()
            .id(ID)
            .type(TYPE_1)
            .unit(Unit.builder().unitId(UNIT_ID).build())
            .issuedBy(
                Staff.builder()
                    .personId(PERSON_ID)
                    .build()
            )
            .externalReference(EXTERNAL_REF)
            .build();

        final var intygId = new IntygId();
        intygId.setExtension(CERTIFICATE_ID);
        EXPECTED_INTYG.setIntygsId(intygId);
        certificate = new Certificate();
        certificate.setMetadata(metadata);
        eventType = HandelsekodEnum.SIGNAT;
        converter = new NotificationMessageMapper();
        registerCertificateType = new RegisterCertificateType();
        registerCertificateType.setIntyg(EXPECTED_INTYG);
        final var marshall = marshall(registerCertificateType);
        xmlRepresentation = Base64.getEncoder().encodeToString(marshall.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shallConvertId() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(ID, result.getIntygsId());
    }

    @Test
    void shallConvertType() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(TYPE_1, result.getIntygsTyp());
    }

    @Test
    void shallSetCurrentTimestamp() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertNotNull(result.getHandelseTid());
    }

    @Test
    void shallConvertEventType() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(eventType, result.getHandelse());
    }

    @Test
    void shallConvertUnitId() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(UNIT_ID, result.getLogiskAdress());
    }

    @Test
    void shallConvertFragorOchSvar() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(FragorOchSvar.getEmpty().getAntalFragor(), result.getFragaSvar().getAntalFragor());
        assertEquals(FragorOchSvar.getEmpty().getAntalSvar(), result.getFragaSvar().getAntalSvar());
        assertEquals(FragorOchSvar.getEmpty().getAntalHanteradeSvar(), result.getFragaSvar().getAntalHanteradeSvar());
        assertEquals(FragorOchSvar.getEmpty().getAntalHanteradeFragor(), result.getFragaSvar().getAntalHanteradeFragor());
    }

    @Test
    void shallConvertMottagnaFragor() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(ArendeCount.getEmpty().getBesvarade(), result.getMottagnaFragor().getBesvarade());
        assertEquals(ArendeCount.getEmpty().getHanterade(), result.getMottagnaFragor().getHanterade());
        assertEquals(ArendeCount.getEmpty().getEjBesvarade(), result.getMottagnaFragor().getEjBesvarade());
        assertEquals(ArendeCount.getEmpty().getTotalt(), result.getMottagnaFragor().getTotalt());
    }

    @Test
    void shallConvertSkickadeFragor() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(ArendeCount.getEmpty().getBesvarade(), result.getSkickadeFragor().getBesvarade());
        assertEquals(ArendeCount.getEmpty().getHanterade(), result.getSkickadeFragor().getHanterade());
        assertEquals(ArendeCount.getEmpty().getEjBesvarade(), result.getSkickadeFragor().getEjBesvarade());
        assertEquals(ArendeCount.getEmpty().getTotalt(), result.getSkickadeFragor().getTotalt());
    }

    @Test
    void shallConvertSchemaVersion() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(SchemaVersion.VERSION_3, result.getVersion());
    }

    @Test
    void shallConvertExternalReference() {
        final var result = converter.map(certificate, xmlRepresentation, eventType);
        assertEquals(EXTERNAL_REF, result.getReference());
    }

    @Nested
    class StatusUpdateXmlTest {

        @Test
        void shallIncludeIntygIdInStatusUpdateXml() {
            final var result = converter.map(certificate, xmlRepresentation, eventType);
            final var careType = unmarshall(result.getStatusUpdateXml());
            assertEquals(EXPECTED_INTYG.getIntygsId().getExtension(), careType.getIntyg().getIntygsId().getExtension());
        }

        @Test
        void shallIncludeHandelseInStatusUpdateXml() {
            final var result = converter.map(certificate, xmlRepresentation, eventType);
            final var careType = unmarshall(result.getStatusUpdateXml());
            assertEquals(eventType.value(), careType.getHandelse().getHandelsekod().getCode());
        }

        @Test
        void shallIncludeHandelseCodeSystemInStatusUpdateXml() {
            final var result = converter.map(certificate, xmlRepresentation, eventType);
            final var careType = unmarshall(result.getStatusUpdateXml());
            assertEquals(KV_HANDELSE_CODE_SYSTEM, careType.getHandelse().getHandelsekod().getCodeSystem());
        }

        @Test
        void shallIncludeHandelseTidpunktInStatusUpdateXml() {
            final var result = converter.map(certificate, xmlRepresentation, eventType);
            final var careType = unmarshall(result.getStatusUpdateXml());
            assertNotNull(careType.getHandelse().getTidpunkt());
        }

        @Test
        void shallIncludeHanteratAvInStatusUpdateXml() {
            final var result = converter.map(certificate, xmlRepresentation, eventType);
            final var careType = unmarshall(result.getStatusUpdateXml());
            assertEquals(PERSON_ID, careType.getHanteratAv().getExtension());
        }
    }


    private static String marshall(RegisterCertificateType registerCertificateType) {
        final var factory = new ObjectFactory();
        final var element = factory.createRegisterCertificate(registerCertificateType);
        try {
            final var context = JAXBContext.newInstance(
                RegisterCertificateType.class,
                DatePeriodType.class
            );
            final var writer = new StringWriter();
            context.createMarshaller().marshal(element, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static CertificateStatusUpdateForCareType unmarshall(byte[] registerCertificateType) {
        final var type = XmlMarshallerHelper.unmarshal(new String(registerCertificateType, StandardCharsets.UTF_8));
        return (CertificateStatusUpdateForCareType) type.getValue();
    }
}
