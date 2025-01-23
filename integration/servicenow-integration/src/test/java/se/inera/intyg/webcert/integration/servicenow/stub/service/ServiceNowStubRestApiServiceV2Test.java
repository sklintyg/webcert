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

package se.inera.intyg.webcert.integration.servicenow.stub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationRequestV2;
import se.inera.intyg.webcert.integration.servicenow.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.servicenow.stub.state.ServiceNowStubState;

@ExtendWith(MockitoExtension.class)
class ServiceNowStubRestApiServiceV2Test {

    @Spy
    private ServiceNowStubState stubState;

    @InjectMocks
    ServiceNowStubRestApiServiceV2 serviceNowStubRestApiServiceV2;

    private static final String BASIC_AUTH = "Basic ";
    private static final List<String> SERVICES = List.of("Webcert-tj", "Webcert-int");
    private static final String SERVICE_CODE_1 = "SERVICE_CODE_1";
    private static final String SERVICE_CODE_2 = "SERVICE_CODE_2";
    private static final String ORGANIZATION_NUMBER_1 = "ORGANIZATION_NUMBER_1";
    private static final String ORGANIZATION_NUMBER_2 = "ORGANIZATION_NUMBER_2";
    private static final List<String> ELEG_SERVICE_CODES = List.of("Webcert fristående med e-legitimation");
    private static final List<String> SITHS_SERVICE_CODES = List.of("Webcert fristående med SITHS-kort", "Webcert Integrerad - via agent",
        "Webcert Integrerad - via region", "Webcert integrerad - direktanslutning");

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(stubState, ServiceNowStubState.class, "elegServiceCodes",
            ELEG_SERVICE_CODES, List.class);
        ReflectionTestUtils.setField(stubState, ServiceNowStubState.class, "sithsServiceCodes",
            SITHS_SERVICE_CODES, List.class);
    }

    @Test
    void shouldReturnOneServiceCodeWhenOneSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            final var serviceCodeList = organizations.getFirst().getServiceCodes();
            assertEquals(1, organizations.size());
            assertEquals(ORGANIZATION_NUMBER_1, organizations.getFirst().getOrganizationNumber());
            assertTrue(serviceCodeList.contains(SERVICE_CODE_1));
        }
    }

    @Test
    void shouldReturnTwoServiceCodesWhenTwoSubscriptionsExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_2))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            final var serviceCodeList = organizations.getFirst().getServiceCodes();
            assertEquals(1, organizations.size());
            assertEquals(ORGANIZATION_NUMBER_2, organizations.getFirst().getOrganizationNumber());
            assertTrue(serviceCodeList.contains(SERVICE_CODE_1));
            assertTrue(serviceCodeList.contains(SERVICE_CODE_2));
        }
    }

    @Test
    void shouldReturnEmptyServiceCodeListWhenNoSubscriptionExists() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of("ORGANIZATION_NUMBER_3"))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            assertEquals(1, organizations.size());
            assertEquals("ORGANIZATION_NUMBER_3", organizations.getFirst().getOrganizationNumber());
            assertTrue(organizations.getFirst().getServiceCodes().isEmpty());
        }
    }

    @Test
    void shouldReturnTwoOrganizationsWithServiceCodesWhenBothHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1, ORGANIZATION_NUMBER_2))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            final var serviceCodeList1 = organizations.getFirst().getServiceCodes();
            final var serviceCodeList2 = organizations.get(1).getServiceCodes();
            assertEquals(2, organizations.size());
            assertEquals(ORGANIZATION_NUMBER_1, organizations.getFirst().getOrganizationNumber());
            assertEquals(ORGANIZATION_NUMBER_2, organizations.get(1).getOrganizationNumber());
            assertEquals(1, serviceCodeList1.size());
            assertEquals(2, serviceCodeList2.size());
        }
    }

    @Test
    void shouldReturnTwoOrganizationsWhenOneHaveSubscription() {
        final var activeSubscriptions = createActiveSubscriptions();
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_2, "ORGANIZATION_NUMBER_3"))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            assertEquals(2, organizations.size());
            assertEquals(ORGANIZATION_NUMBER_2, organizations.getFirst().getOrganizationNumber());
            assertEquals("ORGANIZATION_NUMBER_3", organizations.get(1).getOrganizationNumber());
            assertEquals(2, organizations.getFirst().getServiceCodes().size());
            assertTrue(organizations.get(1).getServiceCodes().isEmpty());
        }
    }

    @Test
    void shouldReturnAllServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsTrue() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = true;
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);
        when(stubState.getServiceCodeList()).thenReturn(List.of(SERVICE_CODE_1, SERVICE_CODE_2, "SERVICE_CODE_3"));

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            assertEquals(1, organizations.size());
            assertEquals(3, organizations.getFirst().getServiceCodes().size());
        }
    }

    @Test
    void shouldReturnNoServiceCodesWhenActiveSubcriptionsIsEmptyAndReturnValueIsFalse() {
        final var activeSubscriptions = new HashMap<String, List<String>>();
        final var setReturnValue = false;
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(0);
        when(stubState.getActiveSubscriptions()).thenReturn(activeSubscriptions);
        when(stubState.getSubscriptionReturnValue()).thenReturn(setReturnValue);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {
            final var organizations = response.readEntity(OrganizationResponse.class).getResult();

            assertEquals(1, organizations.size());
            assertEquals(0, organizations.getFirst().getServiceCodes().size());
        }
    }

    @Test
    void shouldReturnNullErrorResponseWhenErrorSet() {
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(403);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {

            assertEquals(Status.FORBIDDEN, response.getStatusInfo().toEnum());
        }
    }

    @Test
    void shouldReturnError500WhenHttpErrorSetToUnknown() {
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();
        when(stubState.getHttpErrorCode()).thenReturn(777);

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(BASIC_AUTH,
            organizationRequest)) {

            assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatusInfo().toEnum());
        }
    }

    @Test
    void shouldReturnBadRequestIfNoAuthorizationHeader() {
        final var organizationRequest = OrganizationRequestV2.builder()
            .services(SERVICES)
            .customers(List.of(ORGANIZATION_NUMBER_1))
            .build();

        try (final var response = serviceNowStubRestApiServiceV2.createSubscriptionResponse(null,
            organizationRequest)) {

            assertEquals(Status.BAD_REQUEST, response.getStatusInfo().toEnum());
        }
    }

    private Map<String, List<String>> createActiveSubscriptions() {
        return Map.of(
            ORGANIZATION_NUMBER_1, List.of(SERVICE_CODE_1),
            ORGANIZATION_NUMBER_2, List.of(SERVICE_CODE_1, SERVICE_CODE_2)
        );
    }
}