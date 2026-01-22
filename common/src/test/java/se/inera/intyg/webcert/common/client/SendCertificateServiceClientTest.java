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
package se.inera.intyg.webcert.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.modules.converter.InternalConverterUtil;
import se.inera.intyg.common.support.modules.converter.mapping.UnitMapperUtil;
import se.inera.intyg.common.support.modules.converter.mapping.UnitMappingConfigLoader;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

/**
 * Created by eriklupander on 2015-06-04.
 */
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ContextConfiguration(classes = {UnitMappingConfigLoader.class, UnitMapperUtil.class, InternalConverterUtil.class})
class SendCertificateServiceClientTest {

    private static final String INTYGS_ID = "intyg-1";
    private static final String PERSON_ID = "20121212-1212";
    private static final String SKICKAT_AV_JSON = createSkickatAvJson();
    private static final String RECIPIENT = "FKASSA";
    private static final String LOGICAL_ADDRESS = "logical-address-1";

    @Mock
    SendCertificateToRecipientResponderInterface sendService;

    @Mock
    SendCertificateToRecipientResponseType response;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    SendCertificateServiceClientImpl testee = new SendCertificateServiceClientImpl();

    private static String createSkickatAvJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"personId\":\"skapad av pid\",\"fullstandigtNamn\":\"fullständigt namn\",\"forskrivarKod\":");
        sb.append("\"forskrivarKod\",\"befattningar\":[],\"specialiteter\":[],\"vardenhet\":{\"enhetsid\":\"enhetsid\",");
        sb.append("\"enhetsnamn\":\"enhetsnamn\",\"postadress\":\"postadress\",\"postnummer\":\"postNummer\",\"postort\":");
        sb.append("\"postOrt\",\"telefonnummer\":\"telefonNummer\",\"epost\":\"epost\",\"vardgivare\":{\"vardgivarid\":");
        sb.append("\"vardgivarid\",\"vardgivarnamn\":\"vardgivarNamn\"},\"arbetsplatsKod\":\"arbetsplatsKod\"}}");
        return sb.toString();
    }

    @BeforeEach
    void setup() throws Exception {
        lenient().when(objectMapper.readValue(anyString(), eq(HoSPersonal.class))).then(new Answer<HoSPersonal>() {

            @Override
            public HoSPersonal answer(InvocationOnMock invocation) throws Throwable {
                return new ObjectMapper().readValue((String) invocation.getArguments()[0], HoSPersonal.class);
            }
        });
    }

    @Test
    void testSendCertificateOk() {

        when(response.getResult()).thenReturn(buildResultOfCall(ResultCodeType.OK));

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
            .thenReturn(response);
        SendCertificateToRecipientResponseType resp = testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT,
            LOGICAL_ADDRESS);

        assertEquals(ResultCodeType.OK, resp.getResult().getResultCode());
    }

    @Test
    void testSendCertificateNoIntygsId() {
        assertThrows(IllegalArgumentException.class,
            () -> testee.sendCertificate(null, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS));
        verifyNoInteractions(sendService);
    }

    @Test
    void testSendCertificateNoPersonId() {

        assertThrows(IllegalArgumentException.class,
            () -> testee.sendCertificate(INTYGS_ID, null, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS));
        verifyNoInteractions(sendService);
    }

    @Test
    void testSendCertificateNoRecipient() {
        assertThrows(IllegalArgumentException.class,
            () -> testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, null, LOGICAL_ADDRESS));
        verifyNoInteractions(sendService);
    }

    @Test
    void testSendCertificateNoLogicalAddress() {
        assertThrows(IllegalArgumentException.class, () -> testee.sendCertificate(INTYGS_ID, SKICKAT_AV_JSON, PERSON_ID, RECIPIENT, null));
        verifyNoInteractions(sendService);
    }

    /**
     * It's important that the testee is not catching exceptions emitted by the WebService client code. It's up to the
     * caller of the testee to handle exceptions.
     */
    @Test
    void testExceptionsAreForwardedAsIs() {
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
            .thenThrow(new WebServiceException("FOO BAR"));
        assertThrows(WebServiceException.class,
            () -> testee.sendCertificate(INTYGS_ID, PERSON_ID, SKICKAT_AV_JSON, RECIPIENT, LOGICAL_ADDRESS));

        verify(sendService, times(1)).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    private ResultType buildResultOfCall(ResultCodeType resultCodeType) {
        ResultType roc = new ResultType();
        roc.setResultCode(resultCodeType);
        return roc;
    }
}