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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubscriptionRestServiceImpl implements SubscriptionRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionRestServiceImpl.class);

    @Value("${kundportalen.access.token}")
    private String kundportalenAccessToken;

    @Value("${kundportalen.subscriptions.url}")
    private String kundportalenSubscriptionServiceUrl;

    private RestTemplate restTemplate;
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST_MAP_STRING_OBJECT_TYPE
        = new ParameterizedTypeReference<>() { };


    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    @Override
    public List<String> getMissingSubscriptions(Map<String, String> organizationNumberHsaIdMap) {
        final var response = getSubscriptionServiceResponse(organizationNumberHsaIdMap.keySet());

        if (subscriptionServiceCallFailure(response)) {
            return new ArrayList<>();
        }
        return getCareProvidersWithoutSubscription(organizationNumberHsaIdMap, Objects.requireNonNull(response.getBody()));
    }

    @Override
    public boolean isUnregisteredElegUserMissingSubscription(String organizationNumber) {
        final var response = getSubscriptionServiceResponse(Set.of(organizationNumber));

        if (subscriptionServiceCallFailure(response)) {
            return false;
        }
        return ((List<?>) Objects.requireNonNull(response.getBody()).get(0).get("service_code_subscriptions")).isEmpty();
    }

    private ResponseEntity<List<Map<String, Object>>> getSubscriptionServiceResponse(Set<String> organizationNumbers) {
        final var requestEntity = getRequestEntity(organizationNumbers);
        return restTemplate.exchange(kundportalenSubscriptionServiceUrl, HttpMethod.POST, requestEntity, LIST_MAP_STRING_OBJECT_TYPE);
    }

    private HttpEntity<Set<String>> getRequestEntity(Set<String> organizationNumbers) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kundportalenAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(organizationNumbers, headers);
    }

    private List<String> getCareProvidersWithoutSubscription(Map<String, String> orgNumberHsaIdMap, List<Map<String, Object>> response) {
        final var careProvidersMissingSubscription = new ArrayList<String>();
        for (var organization : response) {
            final var organizationNumber = (String) organization.get("org_no");
            final var activeServiceCodes = (List<?>) organization.get("service_code_subscriptions");

            if (activeServiceCodes.isEmpty()) {
                careProvidersMissingSubscription.add(orgNumberHsaIdMap.get(organizationNumber));
            }
        }
        return careProvidersMissingSubscription;
    }

    private boolean subscriptionServiceCallFailure(ResponseEntity<List<Map<String, Object>>> response) {
        // TODO Add monitorlog failed service call (perhaps only for HttpStatus not OK).
        return response.getStatusCode() != HttpStatus.OK || !response.hasBody() || response.getBody() == null;
    }
}
