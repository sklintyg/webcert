/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.subscription;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.schemas.contract.util.HashUtility;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.integration.kundportalen.dto.OrganizationResponse;
import se.inera.intyg.webcert.integration.kundportalen.enumerations.AuthenticationMethodEnum;

@Service
public class SubscriptionRestServiceImpl implements SubscriptionRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionRestServiceImpl.class);

    @Value("${kundportalen.access.token}")
    private String kundportalenAccessToken;

    @Value("${kundportalen.subscriptions.url}")
    private String kundportalenSubscriptionServiceUrl;

    @Value("#{${kundportalen.service.codes.eleg}}")
    private List<String> elegServiceCodes;

    @Value("#{${kundportalen.service.codes.siths}}")
    private List<String> sithsServiceCodes;

    private static final ParameterizedTypeReference<List<OrganizationResponse>> LIST_ORGANIZATION_RESPONSE
        = new ParameterizedTypeReference<>() { };

    private final MonitoringLogService monitoringLogService;
    private final RestTemplate restTemplate;

    public SubscriptionRestServiceImpl(MonitoringLogService monitoringLogService,
        @Qualifier("subscriptionServiceRestTemplate") RestTemplate restTemplate) {
        this.monitoringLogService = monitoringLogService;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<String> getMissingSubscriptions(Map<String, String> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        try {
            final var organizationResponse = getSubscriptionServiceResponse(organizationNumberHsaIdMap.keySet());
            final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody());
            return getCareProvidersMissingSubscription(organizationInfo, organizationNumberHsaIdMap, authMethod);
        } catch (Exception e) {
            errorLogException(organizationNumberHsaIdMap.values(), e);
            monitorLogIfServiceCallFailure(organizationNumberHsaIdMap.values(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        try {
            final var organizationResponse = getSubscriptionServiceResponse(Set.of(organizationNumber));
            final var organizationInfo = Objects.requireNonNull(organizationResponse.getBody());
            return missingSubscription(organizationInfo.get(0).getServiceCodes(), AuthenticationMethodEnum.ELEG);
        } catch (Exception e) {
            errorLogExceptionUnregisteredElegUser(organizationNumber, e);
            monitorLogIfServiceCallFailure(Collections.singleton(HashUtility.hash(organizationNumber)), e);
            return true;
        }
    }

    private ResponseEntity<List<OrganizationResponse>> getSubscriptionServiceResponse(Set<String> organizationNumbers) {
        final var requestEntity = getRequestEntity(organizationNumbers);
        return restTemplate.exchange(kundportalenSubscriptionServiceUrl, HttpMethod.POST, requestEntity, LIST_ORGANIZATION_RESPONSE);
    }

    private HttpEntity<Set<String>> getRequestEntity(Set<String> organizationNumbers) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kundportalenAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(organizationNumbers, headers);
    }

    private List<String> getCareProvidersMissingSubscription(List<OrganizationResponse> organizations,
        Map<String, String> organizationNumberHsaIdMap, AuthenticationMethodEnum authMethod) {
        final var careProvidersMissingSubscription = new ArrayList<String>();

        for (var organization : organizations) {
            final var serviceCodes = organization.getServiceCodes();
            if (missingSubscription(serviceCodes, authMethod)) {
                careProvidersMissingSubscription.add(organizationNumberHsaIdMap.get(organization.getOrganizationNumber()));
            }
        }
        return careProvidersMissingSubscription;
    }

    private boolean missingSubscription(List<String> activeServiceCodes, AuthenticationMethodEnum authMethod) {
        if (activeServiceCodes.isEmpty()) {
            return true;
        }
        if (authMethod == AuthenticationMethodEnum.ELEG) {
            return activeServiceCodes.stream().noneMatch(serviceCode -> elegServiceCodes.contains(serviceCode));
        }
        return activeServiceCodes.stream().noneMatch(serviceCode -> sithsServiceCodes.contains(serviceCode));
    }

    private void errorLogException(Collection<String> hsaIds, Exception e) {
        LOG.error("Kundportalen subscription service call failure for org numbers {}.", hsaIds, e);
    }

    private void errorLogExceptionUnregisteredElegUser(String orgNumber, Exception e) {
        LOG.error("Kundportalen subscription service call failure for unregistered eleg user with org number {}.",
            HashUtility.hash(orgNumber), e);
    }

    private void monitorLogIfServiceCallFailure(Collection<String> queryIds, Exception exception) {
        if (exception instanceof RestClientException) {
            if (exception instanceof RestClientResponseException) {
                final var e = (RestClientResponseException) exception;
                final var timestamp = e.getResponseHeaders() != null
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(e.getResponseHeaders().getDate()), ZoneId.systemDefault()) : null;
                monitorLogRestClientException(queryIds, e.getRawStatusCode(), getStatusText(e.getRawStatusCode()), e.getMessage(),
                    timestamp);
            } else {
                final var e = (RestClientException) exception;
                monitorLogRestClientException(queryIds, null, null, e.getMessage(), null);
            }
        }
    }

    private void monitorLogRestClientException(Collection<String> queryIds, Integer statusCode, String statusText, String exceptionMessage,
        LocalDateTime timestamp) {

        monitoringLogService.logSubscriptionServiceCallFailure(queryIds, statusCode, statusText, exceptionMessage, timestamp);
    }

    private String getStatusText(int rawStatusCode) {
        try {
            return HttpStatus.valueOf(rawStatusCode).name();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
