/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Before;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.infra.integration.hsa.exception.HsaServiceCallException;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsServiceImpl;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.integration.tak.consumer.TakConsumerImpl;
import se.inera.intyg.webcert.integration.tak.model.TakLogicalAddress;
import se.inera.intyg.webcert.integration.tak.model.TakResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private final String SEND_MESSAGE_TO_CARE_NS = "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCare:2";

    private final String ERROR_STRING = "Den angivna enheten går ej att adressera för ärendekommunikation." +
            " (Tjänsten %s är inte registrerad för enhet %s i tjänsteadresseringskatalogen.";

    @Mock
    private HsaOrganizationsServiceImpl hsaService;

    @Mock
    private TakConsumerImpl consumer;

    @InjectMocks
    private TakServiceImpl impl;

    private IntygUser user;

    @Before
    public void setup() {
        impl = new TakServiceImpl();
        ReflectionTestUtils.setField(impl, "timeout", 1);
        MockitoAnnotations.initMocks(this);
        user = createDefaultUser();
        user.setFeatures(ImmutableSet.of(
                WebcertFeature.HANTERA_FRAGOR.getName(),WebcertFeature.HANTERA_FRAGOR.getName() + ".fk7263",
                WebcertFeature.HANTERA_FRAGOR.getName(),WebcertFeature.HANTERA_FRAGOR.getName() + ".luse"
        ));
    }

    @Test
    public void testNoTak() throws HsaServiceCallException {
        setupIds();

        when(consumer.getConnectionPointId()).thenReturn(NTJP_ID);
        when(consumer.getServiceContractId(anyString())).thenReturn("2");

        when(consumer.doLookup(eq(NTJP_ID), eq("NOTOK"), anyString())).thenReturn(new TakLogicalAddress[]{});
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), anyString())).thenReturn(new TakLogicalAddress[]{});
        when(hsaService.getParentUnit("NOTOK")).thenReturn(HSAID_OK);

        assertFalse(impl.verifyTakningForCareUnit("NOTOK", "fk7263", "V1", user).isValid());

        verify(hsaService, times(2)).getParentUnit("NOTOK");

    }

    @Test
    public void testSuccess() throws HsaServiceCallException {
        when(consumer.doLookup(anyString(), anyString(), anyString())).thenReturn(buildTakLogicalAddress("29"));

        assertTrue(impl.verifyTakningForCareUnit("SE2321000198-016965", "fk7263", "V1", user).isValid());
    }

    @Test
    public void testNoTakForArendehanteringRequiredForTsIntyg() {
        setupIds();
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("89"));
        assertTrue(impl.verifyTakningForCareUnit(HSAID_OK, "ts-bas", "V1", user).isValid());
    }

    @Test
    public void testNoTakFoundUseParentHsaUnit() throws HsaServiceCallException {
        setupIds();

        when(consumer.doLookup(eq(NTJP_ID), eq("NOTOK"), anyString())).thenReturn(new TakLogicalAddress[]{});
        when(consumer.doLookup(eq(NTJP_ID), eq(HSAID_OK), anyString())).thenReturn(buildTakLogicalAddress("29"));
        when(hsaService.getParentUnit("NOTOK")).thenReturn(HSAID_OK);

        assertTrue(impl.verifyTakningForCareUnit("NOTOK", "fk7263", "V1", user).isValid());

        verify(hsaService, times(1)).getParentUnit("NOTOK");
    }

    @Test
    public void testSuccessEvenThoughTimeout() throws HsaServiceCallException {
        when(consumer.doLookup(anyString(), anyString(), anyString())).thenAnswer((Answer<TakLogicalAddress[]>) invocation -> {
            Thread.sleep(1500);
            return buildTakLogicalAddress("28");
        });

        assertTrue(impl.verifyTakningForCareUnit("SE2321000198-016965", "fk7263", "V1", user).isValid());
    }

    @Test
    public void testCorrectErrorMessageForReceiveAnswer() {
        setupIds();
        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_QUESTION_ID))).thenReturn(buildTakLogicalAddress("15"));
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));

        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_ANSWER_ID))).thenReturn(new TakLogicalAddress[]{});

        TakResult result = impl.verifyTakningForCareUnit(HSAID_OK, "fk7263", "V1", user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_ANSWER_NS, HSAID_OK), result.getErrorMessages().get(0));
    }


    @Test
    public void testCorrectErrorMessageForReceiveQuestion() {
        setupIds();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));
        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_ANSWER_ID))).thenReturn(buildTakLogicalAddress("18"));

        when(consumer.doLookup(anyString(), anyString(), eq(RECEIVE_CERT_QUESTION_ID))).thenReturn(new TakLogicalAddress[]{});

        String hsa = "SE2321000198-016965";
        TakResult result = impl.verifyTakningForCareUnit(hsa, "fk7263", "V1", user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING, RECEIVE_MEDICAL_CERT_QUESTION_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForSendMessageToCare() {
        setupIds();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V3_ID))).thenReturn(buildTakLogicalAddress("16"));
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(buildTakLogicalAddress("16"));

        when(consumer.doLookup(anyString(), anyString(), eq(SEND_MESSAGE_TO_CARE_ID))).thenReturn(new TakLogicalAddress[]{});

        String hsa = "SE2321000198-016965";
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", "V1", user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING, SEND_MESSAGE_TO_CARE_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForCertStatusUpdateForCareV3() {
        setupIds();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V3_ID))).thenReturn(new TakLogicalAddress[]{});

        String hsa = "SE2321000198-016965";
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", "V3", user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING, CERT_STATUS_FOR_CARE_V3_NS, hsa), result.getErrorMessages().get(0));
    }

    @Test
    public void testCorrectErrorMessageForCertStatusUpdateForCareV1() {
        setupIds();
        when(consumer.doLookup(anyString(), anyString(), eq(CERT_STATUS_V1_ID))).thenReturn(new TakLogicalAddress[]{});

        String hsa = "SE2321000198-016965";
        TakResult result = impl.verifyTakningForCareUnit(hsa, "luse", "V1", user);

        assertTrue(!result.getErrorMessages().isEmpty());
        assertEquals(String.format(ERROR_STRING, CERT_STATUS_FOR_CARE_V1_NS, hsa), result.getErrorMessages().get(0));
    }

    private void setupIds() {
        ReflectionTestUtils.setField(impl, "ntjpId", NTJP_ID);
        ReflectionTestUtils.setField(impl, "certificateStatusUpdateForCareV1Id", CERT_STATUS_V1_ID);
        ReflectionTestUtils.setField(impl, "certificateStatusUpdateForCareV3Id", CERT_STATUS_V3_ID);
        ReflectionTestUtils.setField(impl, "receiveMedicalCertificateQuestionId", RECEIVE_CERT_QUESTION_ID);
        ReflectionTestUtils.setField(impl, "receiveMedicalCertificateAnswerId", RECEIVE_CERT_ANSWER_ID);
        ReflectionTestUtils.setField(impl, "sendMessageToCareId", SEND_MESSAGE_TO_CARE_ID);
    }

    private TakLogicalAddress[] buildTakLogicalAddress(String id) {
        TakLogicalAddress logicalAddress = new TakLogicalAddress();
        logicalAddress.setId(id);
        logicalAddress.setDescription("A description");
        logicalAddress.setLogicalAddress(HSAID_OK);
        return new TakLogicalAddress[] {logicalAddress};
    }

    private IntygUser createDefaultUser() {
        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege("p1",
                        Arrays.asList("fk7263", "ts-bas", "luse"),
                        Arrays.asList(
                                createRequestOrigin(UserOriginType.NORMAL.name(), Arrays.asList("fk7263", "ts-bas", "luse")),
                                createRequestOrigin(UserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
                ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".fk7263",
                        "base_feature"), UserOriginType.NORMAL.name());
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


    private IntygUser createUser(String roleName, Privilege p, Set<String> features, String origin) {
        IntygUser user = new IntygUser("SE2321000198-016965");

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