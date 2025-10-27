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
package se.inera.intyg.webcert.web.service.underskrift.dss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.dss.xsd.dsscore.SignRequest;
import se.inera.intyg.webcert.dss.xsd.dssext.SignRequestExtensionType;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett.SignaturBiljettBuilder;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public class DssSignatureServiceTest {

    public static final String IDP_URL = "https://idpurl.se/samlv2/idp/metadata";

    @Mock
    DssMetadataService dssMetadataService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    DssSignMessageService dssSignMessageService;

    @Mock
    DssSignMessageIdpProvider dssSignMessageIdpProvider;

    @Mock
    WebCertUser user;

    @Mock
    UtkastRepository utkastRepository;

    @Mock
    Optional<Utkast> utkastOptional;

    @Mock
    Utkast utkast;

    @Captor
    ArgumentCaptor<SignRequest> signRequestCaptor;

    @Mock
    Personnummer patient;

    @Mock
    UnderskriftService underskriftService;

    @Mock
    MonitoringLogService monitoringLogService;

    @Mock
    RedisTicketTracker redisTicketTracker;

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Captor
    ArgumentCaptor<String> ticketIdCaptor;

    @Captor
    ArgumentCaptor<String> certificateCaptor;

    @Captor
    ArgumentCaptor<byte[]> signatureCaptor;

    @Captor
    ArgumentCaptor<SignaturStatus> statusCaptor;

    DssSignatureService dssSignatureService;

    static Jaxb2Marshaller marshaller = new Jaxb2Marshaller();


    static {
        String[] packages = {"se.inera.intyg.webcert.dss.xsd"};
        marshaller.setPackagesToScan(packages);

        marshaller.setMarshallerProperties(new HashMap<>() {
            {
                put(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, false);
                put(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
            }
        });
    }


    public DssSignatureServiceTest() {
        MockitoAnnotations.openMocks(this);
        dssSignatureService = new DssSignatureService(dssMetadataService, dssSignMessageService, webCertUserService, utkastRepository,
            dssSignMessageIdpProvider, underskriftService, redisTicketTracker, monitoringLogService, moduleRegistry);
    }

    @Test
    public void createSignatureRequestDTO() throws ModuleNotFoundException {
        when(dssMetadataService.getDssActionUrl()).thenReturn("ActionUrl");
        when(webCertUserService.getUser()).thenReturn(user);
        when(user.getPersonId()).thenReturn("191212121212");
        when(user.getIdentityProviderForSign()).thenReturn(IDP_URL);
        when(dssSignMessageIdpProvider.get(IDP_URL)).thenReturn(IDP_URL);
        when(utkastRepository.findById(Mockito.anyString())).thenReturn(utkastOptional);
        when(utkastOptional.orElse(any())).thenReturn(utkast);
        when(utkast.getIntygsTyp()).thenReturn("intygsTyp");
        when(utkast.getPatientPersonnummer()).thenReturn(patient);
        when(patient.getPersonnummerWithDash()).thenReturn("19121212-1212");
        when(dssSignMessageService.signSignRequest(any()))
            .thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        when(moduleRegistry.getIntygModule(anyString()))
            .thenReturn(new IntygModule("intygsTyp", "intygsTyp", null, null, "intygsTyp", null, null, null, null, "intygsTyp"));

        ReflectionTestUtils.setField(dssSignatureService, "dssClientEntityHostUrl", "https://wc.localtest.me:8020");
        ReflectionTestUtils.setField(dssSignatureService, "dssClientResponseHostUrl", "https://wc.localtest.me:8020");
        ReflectionTestUtils.setField(dssSignatureService, "customerId", "AnUn3ss3c@ary_Long--$@ClientIDwithr4nd0mCharacters");
        ReflectionTestUtils.setField(dssSignatureService, "applicationId", "App/ID\\With--w€|rd__CH@r$");
        ReflectionTestUtils.setField(dssSignatureService, "serviceUrl",
            "https://esign.v2.st.signatureservice.se/signservice-frontend/metadata/4321a111111");
        var signMessageTemplate = "Härmed skriver jag under {intygsTyp} utfärdat för {patientPnr}<br/><br/>Intygs-id: {intygsId}";
        ReflectionTestUtils.setField(dssSignatureService, "signMessage",
            signMessageTemplate);
        ReflectionTestUtils.setField(dssSignatureService, "approvedLoaList", Arrays.asList("http://id.sambi.se/loa/loa2",
            "http://id.sambi.se/loa/loa3"));
        final var signRequestValidityInMinutes = 8;
        ReflectionTestUtils.setField(dssSignatureService, "signRequestValidityInMinutes", signRequestValidityInMinutes);

        var sb = SignaturBiljettBuilder.aSignaturBiljett("ticketId", SignaturTyp.XMLDSIG, SignMethod.SIGN_SERVICE).withIntygsId("intygsId")
            .withHash("HASH").build();

        var createSignatureRequestDTOResponse = dssSignatureService.createSignatureRequestDTO(sb);
        assertNotNull(createSignatureRequestDTOResponse);

        verify(dssSignMessageService).signSignRequest(signRequestCaptor.capture());
        var capturedSignRequest = signRequestCaptor.getValue();

        assertNotNull(capturedSignRequest);
        assertEquals("Profile", "http://id.elegnamnden.se/csig/1.1/dss-ext/profile", capturedSignRequest.getProfile());
        assertNotNull(capturedSignRequest.getInputDocuments());
        assertNotNull(capturedSignRequest.getOptionalInputs());
        assertNotNull(capturedSignRequest.getOptionalInputs().getAny());
        assertEquals(1, capturedSignRequest.getOptionalInputs().getAny().size());

        // Check signMessage
        var signRequestExtensionTypeJAXBElement = (JAXBElement<SignRequestExtensionType>) capturedSignRequest.getOptionalInputs().getAny()
            .getFirst();
        assertNotNull(signRequestExtensionTypeJAXBElement);

        var actualSignMessageBytes = signRequestExtensionTypeJAXBElement.getValue().getSignMessage().getMessage();
        assertNotNull(actualSignMessageBytes);

        var actualSignMessage = new String(actualSignMessageBytes, StandardCharsets.UTF_8);
        var expectedSignMessage = signMessageTemplate.replace("{intygsTyp}", "intygsTyp").replace("{patientPnr}", "19121212-1212")
            .replace("{intygsId}", "intygsId");
        assertEquals(expectedSignMessage, actualSignMessage);

        final var notBefore = signRequestExtensionTypeJAXBElement.getValue().getConditions()
            .getNotBefore().toGregorianCalendar().getTimeInMillis();
        final var notAfter = signRequestExtensionTypeJAXBElement.getValue().getConditions()
            .getNotOnOrAfter().toGregorianCalendar().getTimeInMillis();
        assertEquals("SignRequest should be valid for 10 minutes (2 min before and 8 min after", 10, (notAfter - notBefore) / 60000);
    }

    @Test
    public void receiveSignatureResponse() throws IOException { //TODO Update test case with more accurate sign response
        var stream = new ClassPathResource("dss/signResponse.xml").getInputStream();
        var string = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));

        String relayState = "0ff25a22-d78a-46c0-ae78-58e34b62ce90";
        dssSignatureService.receiveSignResponse(relayState, string);

        verify(underskriftService).netidSignature(ticketIdCaptor.capture(), signatureCaptor.capture(), certificateCaptor.capture());

        assertEquals("0ff25a22-d78a-46c0-ae78-58e34b62ce90", ticketIdCaptor.getValue());

        // CHECKSTYLE:OFF LineLength
        var certByteArray = "MIILpjCCCo6gAwIBAgIILGNiAxjmjgowDQYJKoZIhvcNAQELBQAwgYMxOTA3BgNVBAMMMENHSSBTdmVyaWdlIEFCIFNUIFN1YnN0YW50aWFsIERTUyBFbmQgVXNlciBDQSB2MjEdMBsGA1UECwwURFNTIEVuZCBVc2VyIFNpZ25pbmcxGjAYBgNVBAoMEUNHSSBTdmVyaWdlIEFCIFNUMQswCQYDVQQGEwJTRTAeFw0yMDA1MDcwNDA5NDdaFw0yMTA1MDcxMDAzMzNaMFMxDzANBgNVBCoMBkFuZHJldzEQMA4GA1UEBAwHRGlsbGFyZDEVMBMGA1UEBRMMMTk2NzA3MTgzMTMwMRcwFQYDVQQDDA5BbmRyZXcgRGlsbGFyZDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABE2xWh+y5JLNUYMG69z7feKgXKnK1UIzP6yiaFVofi2RHONk8+Bg9rSX1g5Og65BurXk+2JNv6UwPd9MOk4ShUujggkWMIIJEjAMBgNVHRMBAf8EAjAAME0GA1UdHwRGMEQwQqBAoD6GPGh0dHA6Ly9lc2lnbi52Mi5zdC5zaWduYXR1cmVzZXJ2aWNlLnNlL3Rlc3RDQVN1YnN0YW50aWFsLmNybDAdBgNVHQ4EFgQUtU4TPRik8Zvy1lRTXsP80YfM1AIwHwYDVR0jBBgwFoAU3VXF2mDN4t1bNOpUiJzA4h8SfrswGAYDVR0gBBEwDzANBgsrBgEEAYH1fgMIAjAOBgNVHQ8BAf8EBAMCBkAwgghHBgcqhXCBSQUBBIIIOjCCCDYwgggyDCtodHRwOi8vaWQuZWxlZ25hbW5kZW4uc2UvYXV0aC1jb250LzEuMC9zYWNpDIIIATxzYWNpOlNBTUxBdXRoQ29udGV4dCB4bWxuczpzYWNpPSJodHRwOi8vaWQuZWxlZ25hbW5kZW4uc2UvYXV0aC1jb250LzEuMC9zYWNpIiB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyIgeG1sbnM6eGVuYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjIiB4bWxuczpzYW1sPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48c2FjaTpBdXRoQ29udGV4dEluZm8gSWRlbnRpdHlQcm92aWRlcj0iaHR0cHM6Ly90ZXN0aWRwLnYyLnNpZ25hdHVyZXNlcnZpY2Uuc2Uvc2FtbHYyL2lkcC9tZXRhZGF0YSIgQXV0aGVudGljYXRpb25JbnN0YW50PSIyMDIwLTA1LTA3VDA2OjE0OjQ3LjM4OCswMjowMCIgQXV0aG5Db250ZXh0Q2xhc3NSZWY9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0IiBBc3NlcnRpb25SZWY9Il83MDM5ZjBkMjIyNzVmNDFhZDEyYTU0MDZkYmFhNGE2YSIvPjxzYWNpOklkQXR0cmlidXRlcz48c2FjaTpBdHRyaWJ1dGVNYXBwaW5nIFR5cGU9InJkbiIgUmVmPSIyLjUuNC40MiI+PHNhbWw6QXR0cmlidXRlIE5hbWU9InVybjpvaWQ6Mi41LjQuNDIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhzaTp0eXBlPSJ4czpzdHJpbmciPkFuZHJldzwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjwvc2FjaTpBdHRyaWJ1dGVNYXBwaW5nPjxzYWNpOkF0dHJpYnV0ZU1hcHBpbmcgVHlwZT0icmRuIiBSZWY9IjIuNS40LjQiPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJ1cm46b2lkOjIuNS40LjQiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhzaTp0eXBlPSJ4czpzdHJpbmciPkRpbGxhcmQ8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48L3NhY2k6QXR0cmlidXRlTWFwcGluZz48c2FjaTpBdHRyaWJ1dGVNYXBwaW5nIFR5cGU9InJkbiIgUmVmPSIyLjUuNC41Ij48c2FtbDpBdHRyaWJ1dGUgTmFtZT0idXJuOm9pZDoxLjIuNzUyLjI5LjQuMTMiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhzaTp0eXBlPSJ4czpzdHJpbmciPjE5NjcwNzE4MzEzMDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjwvc2FjaTpBdHRyaWJ1dGVNYXBwaW5nPjxzYWNpOkF0dHJpYnV0ZU1hcHBpbmcgVHlwZT0icmRuIiBSZWY9IjIuNS40LjMiPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJ1cm46b2lkOjIuMTYuODQwLjEuMTEzNzMwLjMuMS4yNDEiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIHhzaTp0eXBlPSJ4czpzdHJpbmciPkFuZHJldyBEaWxsYXJkPC9zYW1sOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDpBdHRyaWJ1dGU+PC9zYWNpOkF0dHJpYnV0ZU1hcHBpbmc+PC9zYWNpOklkQXR0cmlidXRlcz48L3NhY2k6U0FNTEF1dGhDb250ZXh0PjANBgkqhkiG9w0BAQsFAAOCAQEAmDwgUV0PaZ/pIBrlx4vEIXoiqrG6s2PhSxGX5BbBhL08uCMM7Bempv/1mxueXfXksooQvEMe7zO3TdjtB0i4oIYwORQp2A71Y8aBb/cLIt/KAarS3q/1tYs3ui/EuIPRGcRD5pbfZvLkdEgBlU+cEMh5uvApCI4gfPwbqjCDwitjqnKZRGwt6z/+2zfuuqvXWeZDBkoUsWXXHYtoWXLheGruOdk8Eh2YlseqqtSSi7CKwkSxROdea9+0XBjj7pzrKhuXSpMJ6plo0GvJJV1fPrgH8xPWXVxM1NF8i4UXgIxKwmRZmAtk0c6erKGqhOVztegaobde3ontsoEyl/UBLQ==";
        // CHECKSTYLE:ON LineLength
        assertEquals(certByteArray, certificateCaptor.getValue());

        var signByteArray = Base64.getDecoder()
            .decode("MEYCIQDexXhdoTJWwjcXnQUGR2QAXcEbP+5N1f8QPghNeGb2dgIhAOvoNNfTlZqPyhuuYwLryVirmx/90NHsWf+oefZEQjBd");
        assertEquals(Arrays.toString(signatureCaptor.getValue()), Arrays.toString(signByteArray));
    }

    @Test
    public void findReturnUrl() {
        String intygsId = UUID.randomUUID().toString();
        Utkast utkastLocal = new Utkast();
        utkastLocal.setIntygsTyp("lisjp");
        utkastLocal.setIntygTypeVersion("1.1");

        ReflectionTestUtils.setField(dssSignatureService, "dssClientEntityHostUrl", "https://wc.localtest.me:8020");
        ReflectionTestUtils.setField(dssSignatureService, "dssClientResponseHostUrl", "https://wc.localtest.me:8020");
        when(utkastRepository.findById(anyString())).thenReturn(utkastOptional);
        when(utkastOptional.isPresent()).thenReturn(true);
        when(utkastOptional.get()).thenReturn(utkastLocal);

        String returnUrl = dssSignatureService.findReturnUrl(intygsId);

        assertEquals(String.format("https://wc.localtest.me:8020/#/intyg/lisjp/1.1/%s/?signed", intygsId), returnUrl);

    }

    @Test
    public void findReturnErrorUrl() {
        String intygsId = UUID.randomUUID().toString();
        Utkast utkastLocal = new Utkast();
        utkastLocal.setIntygsTyp("lisjp");
        utkastLocal.setIntygTypeVersion("1.1");

        ReflectionTestUtils.setField(dssSignatureService, "dssClientEntityHostUrl", "https://wc.localtest.me:8020");
        ReflectionTestUtils.setField(dssSignatureService, "dssClientResponseHostUrl", "https://wc.localtest.me:8020");
        when(utkastRepository.findById(anyString())).thenReturn(utkastOptional);
        when(utkastOptional.isPresent()).thenReturn(true);
        when(utkastOptional.get()).thenReturn(utkastLocal);

        String returnUrl = dssSignatureService.findReturnErrorUrl(intygsId, "123");

        assertEquals(String.format("https://wc.localtest.me:8020/#/lisjp/1.1/edit/%s/?error&ticket=123", intygsId), returnUrl);

    }

    @Test
    public void updateSignatureTicketWithError() {

        String relayState = "TransactionID";

        dssSignatureService.updateSignatureTicketWithError(relayState);

        verify(redisTicketTracker).updateStatus(ticketIdCaptor.capture(), statusCaptor.capture());

        assertEquals(relayState, ticketIdCaptor.getValue());
        assertEquals(SignaturStatus.ERROR, statusCaptor.getValue());
    }

    @Test
    public void isUnitInIeWhitelist() {
        assertTrue(dssSignatureService.shouldUseSigningService(""));
        assertTrue(dssSignatureService.shouldUseSigningService(null));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-1077"));

        // Just one empty post in whitelist
        ReflectionTestUtils
            .setField(dssSignatureService, "dssUnitWhitelistForIe", List.of(""));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-1077"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT23210001512-WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT23210001512WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("FINNS_INTE"));

        // Shall return true for all units if wildcare * is used
        ReflectionTestUtils
            .setField(dssSignatureService, "dssUnitWhitelistForIe", List.of("*"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-1077"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT23210001512-WILDCARD"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT23210001512WILDCARD"));
        assertFalse(dssSignatureService.shouldUseSigningService("FINNS_INTE"));

        // One strict and one wildcard unit in whitelist
        ReflectionTestUtils
            .setField(dssSignatureService, "dssUnitWhitelistForIe", Arrays.asList("TSTNMT2321000156-1077", "TSTNMT23210001512-*"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-1077"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-107"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-10777"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT23210001512-WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT23210001512WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("FINNS_INTE"));

        // Mixed upper and lowercase in whitelist and currentCareUnit
        // One strict and one wildcard unit in whitelist
        ReflectionTestUtils
            .setField(dssSignatureService, "dssUnitWhitelistForIe", Arrays.asList("tStNmT2321000156-1077", "tStNmT23210001512-*"));
        assertFalse(dssSignatureService.shouldUseSigningService("TsTnMt2321000156-1077"));
        assertFalse(dssSignatureService.shouldUseSigningService("TsTnMt23210001512-WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("TsTnMt23210001512WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("FiNNS_iNTE"));

        // One wildcard value in whitelist
        ReflectionTestUtils
            .setField(dssSignatureService, "dssUnitWhitelistForIe", List.of("TSTNMT23210001512*"));
        assertTrue(dssSignatureService.shouldUseSigningService("TSTNMT2321000156-1077"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT23210001512-WILDCARD"));
        assertFalse(dssSignatureService.shouldUseSigningService("TSTNMT23210001512WILDCARD"));
        assertTrue(dssSignatureService.shouldUseSigningService("FINNS_INTE"));
    }
}
