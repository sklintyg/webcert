/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.tak.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsServiceImpl;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumerImpl;
import se.inera.intyg.webcert.integration.tak.consumer.TakServiceException;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TakServiceImplTest {

    private static final String NTJP_ID = "1";
    private static final String HSAID_OK = "SE2321000198-016965";

    private static final String CERT_STATUS_V1_ID = "2";
    private static final String CERT_STATUS_V3_ID = "3";
    private static final String RECEIVE_CERT_QUESTION_ID = "4";
    private static final String RECEIVE_CERT_ANSWER_ID = "5";
    private static final String SEND_MESSAGE_TO_CARE_ID = "6";

    private final String CERT_STATUS_FOR_CARE_V1_NS = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:1";
    private final String CERT_STATUS_FOR_CARE_V3_NS = "urn:riv:clinicalprocess:healthcond:certificate:CertificateStatusUpdateForCareResponder:3";
    private final String RECEIVE_MEDICAL_CERT_QUESTION_NS = "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateQuestionResponder:1";
    private final String RECEIVE_MEDICAL_CERT_ANSWER_NS = "urn:riv:insuranceprocess:healthreporting:ReceiveMedicalCertificateAnswerResponder:1";
    private final String SEND_MESSAGE_TO_CARE_NS = "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:2";

    private final String ERROR_STRING_BASE = "Tjänsten %s är inte registrerad för enhet %s i tjänsteadresseringskatalogen.";

    private final String ERROR_STRING_ARENDEHANTERING = "Den angivna enheten går ej att adressera för ärendekommunikation. "
            + ERROR_STRING_BASE;

    @Mock
    private HsaOrganizationsServiceImpl hsaService;

    @Mock
    private TakConsumerImpl consumer;

    @InjectMocks
    private TakServiceImpl impl;

    private IntygUser user;

    @Before
    public void setup() throws HsaServiceCallException {
        impl = new TakServiceImpl();
        ReflectionTestUtils.setField(impl, "timeout", 1_000_000);
        MockitoAnnotations.initMocks(this);
        user = createDefaultUser();
        when(hsaService.getParentUnit(HSAID_OK)).thenReturn(HSAID_OK);
    }

    @Test
    public void testNoTak() throws HsaServiceCallException, TakServiceException {
        setupIds();

        when(consumer.getConnectionPointId()).thenReturn(NTJP_ID);
        when(consumer.getServiceContractId(anyString())).thenReturn("2");

        when(consumer.doLookup(eq(NTJP_ID), eq("NOTOK"), anyString())).thenReturn(new TakLogicalAddress[] {});
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), anyString())).thenReturn(new TakLogicalAddress[] {});
        when(hsaService.getParentUnit("NOTOK")).thenReturn(HSAID_OK);

        assertFalse(impl.verifyTakningForCareUnit("NOTOK", "fk7263", SchemaVersion.VERSION_1, user).isValid());

        verify(hsaService, times(2)).getParentUnit("NOTOK");

    }

    @Test
    public void testSuccess() {
        doReturn(buildTakLogicalAddress("29")).when(consumer).doLookup(isNull(), anyString(), isNull());
        assertTrue(impl.verifyTakningForCareUnit(HSAID_OK, "fk7263", SchemaVersion.VERSION_1, user).isValid());
    }

    @Test
    public void testSuccessForDifferentUnitsFk7263() throws HsaServiceCallException {
        setupIds();

        final String HSAID_OK_VARDENHET = "HSAID_OK_VARDENHET";
        final String HSAID_OK_VARDGIVARE = "HSAID_OK_VARDGIVARE";

        when(hsaService.getParentUnit(HSAID_OK)).thenReturn(HSAID_OK_VARDENHET);
        when(hsaService.getVardgivareOfVardenhet(HSAID_OK)).thenReturn(HSAID_OK_VARDGIVARE);

        when(consumer.doLookup(any(), eq(HSAID_OK), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("29"));
        when(consumer.doLookup(any(), eq(HSAID_OK_VARDENHET), eq(RECEIVE_CERT_ANSWER_ID))).thenReturn(buildTakLogicalAddress("29"));
        when(consumer.doLookup(any(), eq(HSAID_OK_VARDGIVARE), eq(RECEIVE_CERT_QUESTION_ID))).thenReturn(buildTakLogicalAddress("29"));
        assertTrue(impl.verifyTakningForCareUnit(HSAID_OK, "fk7263", SchemaVersion.VERSION_1, user).isValid());
    }

    @Test
    public void testSuccessForDifferentUnitsSMI() throws HsaServiceCallException {
        setupIds();

        final String HSAID_OK_VARDENHET = "HSAID_OK_VARDENHET";
        final String HSAID_OK_VARDGIVARE = "HSAID_OK_VARDGIVARE";

        when(hsaService.getParentUnit(HSAID_OK)).thenReturn(HSAID_OK_VARDENHET);
        when(hsaService.getVardgivareOfVardenhet(HSAID_OK)).thenReturn(HSAID_OK_VARDGIVARE);

        when(consumer.doLookup(any(), eq(HSAID_OK), eq(CERT_STATUS_V3_ID))).thenReturn(buildTakLogicalAddress("29"));
        when(consumer.doLookup(any(), eq(HSAID_OK_VARDGIVARE), eq(SEND_MESSAGE_TO_CARE_ID))).thenReturn(buildTakLogicalAddress("29"));
        assertTrue(impl.verifyTakningForCareUnit(HSAID_OK, "luse", SchemaVersion.VERSION_3, user).isValid());
    }

    @Test
    public void testNoTakForArendehanteringRequiredForTsIntyg() {
        setupIds();
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("89"));
        assertTrue(impl.verifyTakningForCareUnit(HSAID_OK, "ts-bas", SchemaVersion.VERSION_1, user).isValid());
    }

    @Test
    public void testNoTakFoundUseParentHsaUnit() throws HsaServiceCallException {
        setupIds();

        when(consumer.doLookup(eq(NTJP_ID), eq("NOTOK"), anyString())).thenReturn(new TakLogicalAddress[] {});
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), anyString())).thenReturn(buildTakLogicalAddress("29"));
        when(hsaService.getParentUnit("NOTOK")).thenReturn(HSAID_OK);

        assertTrue(impl.verifyTakningForCareUnit("NOTOK", "fk7263", SchemaVersion.VERSION_1, user).isValid());

        verify(hsaService, times(1)).getParentUnit("NOTOK");
    }

    @Test
    public void testSuccessEvenThoughTimeout() {
        when(consumer.doLookup(isNull(), anyString(), isNull())).thenAnswer((Answer<TakLogicalAddress[]>) invocation -> {
            Thread.sleep(1500);
            return buildTakLogicalAddress("28");
        });

        boolean result = impl.verifyTakningForCareUnit(HSAID_OK, "fk7263", SchemaVersion.VERSION_1, user).isValid();

        assertTrue(result);
    }

    @Test
    public void testCorrectErrorMessageForReceiveAnswer() {
        setupIds();
        setupMockUpdate();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));

        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_ANSWER_ID))).thenReturn(new TakLogicalAddress[] {});

        TakResult result = impl.verifyTakningForCareUnit(HSAID_OK, "fk7263", SchemaVersion.VERSION_1, user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING_ARENDEHANTERING, RECEIVE_MEDICAL_CERT_ANSWER_NS, HSAID_OK),
                result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForReceiveQuestion() {
        setupIds();
        setupMockUpdate();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));
        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_ANSWER_ID))).thenReturn(buildTakLogicalAddress("18"));

        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_QUESTION_ID))).thenReturn(new TakLogicalAddress[] {});

        String hsa = HSAID_OK;
        TakResult result = impl.verifyTakningForCareUnit(hsa, "fk7263", SchemaVersion.VERSION_1, user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING_ARENDEHANTERING, RECEIVE_MEDICAL_CERT_QUESTION_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForSendMessageToCare() {
        setupIds();
        setupMockUpdate();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));

        when(consumer.doLookup(anyString(), anyString(), eq(SEND_MESSAGE_TO_CARE_ID))).thenReturn(new TakLogicalAddress[] {});

        String hsa = HSAID_OK;
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", SchemaVersion.VERSION_1, user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING_ARENDEHANTERING, SEND_MESSAGE_TO_CARE_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForCertStatusUpdateForCareV3() throws TakServiceException {
        setupIds();
        setupMockUpdate();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V3_ID))).thenReturn(new TakLogicalAddress[] {});

        String hsa = HSAID_OK;
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", SchemaVersion.VERSION_3, user);

        System.out.println(result.getErrorMessages().size());

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING_BASE, CERT_STATUS_FOR_CARE_V3_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForCertStatusUpdateForCareV1() throws TakServiceException {
        setupIds();
        setupMockUpdate();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(new TakLogicalAddress[] {});

        String hsa = HSAID_OK;
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", SchemaVersion.VERSION_1, user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING_BASE, CERT_STATUS_FOR_CARE_V1_NS, hsa), result.getErrorMessages().get(0));
    }

    private void setupIds() {
        ReflectionTestUtils.setField(impl, "ntjpId", NTJP_ID);
        ReflectionTestUtils.setField(impl, "certificateStatusUpdateForCareV1Id", CERT_STATUS_V1_ID);
        ReflectionTestUtils.setField(impl, "certificateStatusUpdateForCareV3Id", CERT_STATUS_V3_ID);
        ReflectionTestUtils.setField(impl, "receiveMedicalCertificateQuestionId", RECEIVE_CERT_QUESTION_ID);
        ReflectionTestUtils.setField(impl, "receiveMedicalCertificateAnswerId", RECEIVE_CERT_ANSWER_ID);
        ReflectionTestUtils.setField(impl, "sendMessageToCareId", SEND_MESSAGE_TO_CARE_ID);
    }

    private void setupMockUpdate() {
        when(consumer.getConnectionPointId()).thenReturn(NTJP_ID);
        when(consumer.getServiceContractId(CERT_STATUS_FOR_CARE_V1_NS)).thenReturn(CERT_STATUS_V1_ID);
        when(consumer.getServiceContractId(CERT_STATUS_FOR_CARE_V3_NS)).thenReturn(CERT_STATUS_V3_ID);
        when(consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_QUESTION_NS)).thenReturn(RECEIVE_CERT_QUESTION_ID);
        when(consumer.getServiceContractId(RECEIVE_MEDICAL_CERT_ANSWER_NS)).thenReturn(RECEIVE_CERT_ANSWER_ID);
        when(consumer.getServiceContractId(SEND_MESSAGE_TO_CARE_NS)).thenReturn(SEND_MESSAGE_TO_CARE_ID);
    }

    private TakLogicalAddress[] buildTakLogicalAddress(String id) {
        TakLogicalAddress logicalAddress = new TakLogicalAddress();
        logicalAddress.setId(id);
        logicalAddress.setDescription("A description");
        logicalAddress.setLogicalAddress(HSAID_OK);
        return new TakLogicalAddress[] { logicalAddress };
    }

    private IntygUser createDefaultUser() {
        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege("p1",
                        Arrays.asList("fk7263", "ts-bas", "luse"),
                        Arrays.asList(
                                createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList("fk7263", "ts-bas", "luse")),
                                createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Collections.singletonList("ts-bas")))),
                Stream.of(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, AuthoritiesConstants.FEATURE_TAK_KONTROLL_TRADKLATTRING)
                        .collect(Collectors.toMap(Function.identity(), s -> {
                            Feature feature = new Feature();
                            feature.setName(s);
                            feature.setGlobal(true);
                            feature.setIntygstyper(Arrays.asList("fk7263", "luse"));
                            return feature;
                        })), UserOriginType.NORMAL.name());
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin o = new RequestOrigin();
        o.setName(name);
        o.setIntygstyper(intygstyper);
        return o;
    }

    private Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege p = new Privilege();
        p.setName(name);
        p.setIntygstyper(intygsTyper);
        p.setRequestOrigins(requestOrigins);
        return p;
    }

    private IntygUser createUser(String roleName, Privilege p, Map<String, Feature> features, String origin) {
        IntygUser user = new IntygUser(HSAID_OK);

        HashMap<String, Privilege> pMap = new HashMap<>();
        pMap.put(p.getName(), p);
        user.setAuthorities(pMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> rMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        rMap.put(roleName, role);

        user.setRoles(rMap);
        return user;
    }

}
