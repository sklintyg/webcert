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
package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.buildIntygXMLSignature;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createSignaturBiljett;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createUtkast;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createVardperson;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.xmldsig.model.TransformAndDigestResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResponse;
import se.inera.intyg.infra.xmldsig.model.ValidationResult;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.infra.xmldsig.service.XMLDSigServiceImpl;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class XmlUnderskriftServiceImplTest {

    private static final String INTYG_ID = "intyg-1";
    private static final String INTYG_TYP = "luse";
    private static final String ENHET_ID = "enhet-1";
    private static final String PERSON_ID = "19121212-1212";
    private static final String TICKET_ID = "ticket-1";
    private static final Long VERSION = 1L;

    private static final ValidationResponse okValidationResult = ValidationResponse.ValidationResponseBuilder.aValidationResponse()
        .withSignatureValid(ValidationResult.OK).withReferencesValid(ValidationResult.OK).build();

    private static final ValidationResponse failedValidationResult = ValidationResponse.ValidationResponseBuilder.aValidationResponse()
        .withSignatureValid(ValidationResult.INVALID).withReferencesValid(ValidationResult.NOT_CHCEKED).build();
    private static final byte[] SIGNATUR_BYTES = "signatur".getBytes(StandardCharsets.UTF_8);

    @Mock
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Mock
    private PrepareSignatureService prepareSignatureService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private RedisTicketTracker redisTicketTracker;

    @Mock
    private XMLDSigServiceImpl xmldSigService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private UtkastRepository utkastRepository;
    @Mock
    private SignCertificateService signCertificateService;

    @Mock
    private IntygService intygService;

    @InjectMocks
    private XmlUnderskriftServiceImpl testee;

    @Before
    public void init() throws ModuleNotFoundException, ModuleException {
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleApi.updateAfterSigning(anyString(), anyString())).thenReturn("json");

        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
    }

    @Test
    public void testSigneringsBiljettMedDigest() {
        when(prepareSignatureService.prepareSignature(anyString(), anyString(), anyString())).thenReturn(buildIntygXMLSignature());

        SignaturBiljett signaturBiljett = testee.skapaSigneringsBiljettMedDigest(INTYG_ID, INTYG_TYP, VERSION, Optional.of("json"),
            SignMethod.FAKE,
            TICKET_ID, "<xml/>");
        assertNotNull(signaturBiljett);
        verify(redisTicketTracker, times(1)).trackBiljett(any(SignaturBiljett.class));
    }

    @Test
    public void testFinalizeSignatureWithValidSignature() {
        when(xmldSigService.buildKeyInfoForCertificate(anyString())).thenReturn(new ObjectFactory().createKeyInfoType());
        when(prepareSignatureService.encodeSignatureIntoSignedXml(any(SignatureType.class), anyString())).thenReturn("<final-xml/>");
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("json");

        when(xmldSigService.validateSignatureValidity(anyString(), anyBoolean())).thenReturn(okValidationResult);
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("<xml/>");
        when(prepareSignatureService.transformAndGenerateDigest(anyString(), anyString()))
            .thenReturn(new TransformAndDigestResponse(null,
                Base64.getEncoder().encode(
                    UnderskriftTestUtil.buildIntygXMLSignature().getSignatureType().getSignedInfo().getReference().get(0)
                        .getDigestValue())));
        when(utkastRepository.save(any(Utkast.class)))
            .thenReturn(createUtkast(INTYG_ID, 2L, INTYG_TYP, UtkastStatus.SIGNED, "model", createVardperson(),
                ENHET_ID, PERSON_ID));
        when(redisTicketTracker.updateStatus(TICKET_ID, SignaturStatus.SIGNERAD))
            .thenReturn(createSignaturBiljett(SignaturStatus.SIGNERAD));

        WebCertUser user = mock(WebCertUser.class);
        when(user.getAuthenticationScheme()).thenReturn("scheme");
        when(user.getHsaId()).thenReturn("user-1");
        SignaturBiljett signaturBiljett = testee.finalizeSignature(createSignaturBiljett(SignaturStatus.BEARBETAR),
            "signatur".getBytes(Charset.forName("UTF-8")),
            "certifikat", createUtkast(INTYG_ID, 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", createVardperson(),
                ENHET_ID, PERSON_ID),
            user);
        assertNotNull(signaturBiljett);
        verify(monitoringLogService, times(1)).logIntygSigned(anyString(), anyString(), eq("user-1"), eq("scheme"),
            ArgumentMatchers.isNull());
        verify(redisTicketTracker, times(1)).updateStatus(anyString(), eq(SignaturStatus.SIGNERAD));
        verify(intygService, times(1)).storeIntyg(any(Utkast.class));
    }

    @Test
    public void testFinalizeSignatureWithValidSignatureForCs() {
        final var certificate = new Certificate();
        final var ticket = createSignaturBiljett(SignaturStatus.BEARBETAR);
        final var expectedResponse = FinalizedCertificateSignature.builder()
            .certificate(certificate)
            .signaturBiljett(ticket)
            .build();

        when(xmldSigService.buildKeyInfoForCertificate(anyString())).thenReturn(new ObjectFactory().createKeyInfoType());
        when(signCertificateService.sign(eq(ticket.getIntygsId()), anyString(), eq(ticket.getVersion()))).thenReturn(certificate);
        when(redisTicketTracker.updateStatus(TICKET_ID, SignaturStatus.SIGNERAD))
            .thenReturn(createSignaturBiljett(SignaturStatus.SIGNERAD));

        final var actualResult = testee.finalizeSignatureForCS(
            ticket,
            SIGNATUR_BYTES,
            "certifikat"
        );
        assertEquals(expectedResponse, actualResult);
        verify(redisTicketTracker, times(1)).updateStatus(ticket.getTicketId(), SignaturStatus.SIGNERAD);
    }

    @Test(expected = WebCertServiceException.class)
    public void testFinalizeSignatureFailsWithDifferentVersions() {
        when(xmldSigService.buildKeyInfoForCertificate(anyString())).thenReturn(new ObjectFactory().createKeyInfoType());
        when(prepareSignatureService.encodeSignatureIntoSignedXml(any(SignatureType.class), anyString())).thenReturn("<final-xml/>");
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("json");
        when(xmldSigService.validateSignatureValidity(anyString(), anyBoolean())).thenReturn(okValidationResult);
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("<xml/>");
        when(prepareSignatureService.transformAndGenerateDigest(anyString(), anyString()))
            .thenReturn(new TransformAndDigestResponse(null,
                Base64.getEncoder().encode(
                    UnderskriftTestUtil.buildIntygXMLSignature().getSignatureType().getSignedInfo().getReference().get(0)
                        .getDigestValue())));

        try {
            testee.finalizeSignature(createSignaturBiljett(SignaturStatus.BEARBETAR),
                "signatur".getBytes(Charset.forName("UTF-8")),
                "certifikat", createUtkast(INTYG_ID, 1111L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", createVardperson(),
                    ENHET_ID, PERSON_ID),
                buildUser());
        } finally {
            verifyNoInteractions(monitoringLogService);
            verify(redisTicketTracker, times(1)).updateStatus(anyString(), eq(SignaturStatus.OKAND));
            verify(intygService, times(0)).storeIntyg(any(Utkast.class));
        }
    }

    private WebCertUser buildUser() {
        return new WebCertUser();
    }

    @Test(expected = WebCertServiceException.class)
    public void testFinalizeSignatureFailsWithDifferentIntygsId() {
        when(xmldSigService.buildKeyInfoForCertificate(anyString())).thenReturn(new ObjectFactory().createKeyInfoType());
        when(prepareSignatureService.encodeSignatureIntoSignedXml(any(SignatureType.class), anyString())).thenReturn("<final-xml/>");
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("json");
        when(xmldSigService.validateSignatureValidity(anyString(), anyBoolean())).thenReturn(okValidationResult);
        when(utkastModelToXMLConverter.utkastToXml(anyString(), anyString())).thenReturn("<xml/>");

        try {
            testee.finalizeSignature(createSignaturBiljett(SignaturStatus.BEARBETAR),
                "signatur".getBytes(Charset.forName("UTF-8")),
                "certifikat",
                createUtkast(INTYG_ID + "-difference", 1L, INTYG_TYP, UtkastStatus.DRAFT_COMPLETE, "model", createVardperson(),
                    ENHET_ID, PERSON_ID),
                buildUser());
        } finally {
            verifyNoInteractions(monitoringLogService);
            verify(redisTicketTracker, times(1)).updateStatus(anyString(), eq(SignaturStatus.OKAND));
            verify(intygService, times(0)).storeIntyg(any(Utkast.class));
        }
    }

}