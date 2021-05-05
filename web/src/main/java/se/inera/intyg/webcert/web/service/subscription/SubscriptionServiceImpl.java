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

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionAction;
import se.inera.intyg.infra.security.common.model.UserOriginType;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    @Value("${kundportalen.access.token}")
    private String kundportalenAccessToken;

    @Value("${kundportalen.subscriptions.url}")
    private String kundportalenSubscriptionServiceUrl;

    @Value("#{${kundportalen.service.codes.eleg}}")
    private List<String> kundportalenElegServiceCodes;

    @Value("#{${kundportalen.service.codes.siths}}")
    private List<String> kundportalenSithsServiceCodes;

    private static final ParameterizedTypeReference<Map<String, Boolean>> MAP_STRING_BOOLEAN_TYPE = new ParameterizedTypeReference<>() { };

    @Override
    public SubscriptionInfo fetchSubscriptionInfo(WebCertUser webCertUser) {
        final var missingSubscriptionAction = determineSubscriptionAction(webCertUser.getOrigin(), webCertUser.getFeatures());
        if (missingSubscriptionAction != SubscriptionAction.NONE_SUBSCRIPTION_FEATURES_NOT_ACTIVE) {
            final var httpEntity = getHttpEntity();
            final var restTemplate = new RestTemplate();
            final var careProviderOrgNumbers = getCareProviderOrgNumbers(webCertUser);
            final var serviceCodes = getRelevantServiceCodes(webCertUser);
            final var careProviderHsaIds = getMissingSubscriptions(httpEntity, restTemplate, careProviderOrgNumbers, serviceCodes);
            return new SubscriptionInfo(missingSubscriptionAction, careProviderHsaIds);
        }
        return SubscriptionInfo.createSubscriptionInfoFeaturesNotActive();
    }

    @Override
    public SubscriptionAction determineSubscriptionAction(String origin, Map<String, Feature> features) {
        if (isFristaendeWebcertUser(origin)) {
            if (isPastSubscriptionAdjustmentPeriod(features)) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_BLOCK;
            } else if (isDuringSubscriptionAdjustmentPeriod(features)) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_WARN;
            }
        }
        return SubscriptionAction.NONE_SUBSCRIPTION_FEATURES_NOT_ACTIVE;
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kundportalenAccessToken);
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    private Map<String, String> getCareProviderOrgNumbers(WebCertUser webCertUser) {
        if (isPrivatePractitioner(webCertUser) && isElegUser(webCertUser)) {
            final var careProvider = webCertUser.getVardgivare().stream().findFirst().map(Vardgivare::getId).orElse("NOT_FOUND");
            final var orgNumber = extractOrgNumberFromPersonId(webCertUser.getPersonId());
            return Map.of(careProvider, orgNumber);
        } else {
            final var careProviderOrgNumbers = new HashMap<String, String>();
            for (var careProvider : webCertUser.getVardgivare()) {
                careProviderOrgNumbers.put(careProvider.getId(), extractOrgNumberFromCareUnits(careProvider.getVardenheter()));
            }
            return careProviderOrgNumbers;
        }
    }

    private List<String> getMissingSubscriptions(HttpEntity<String> httpEntity, RestTemplate restTemplate,
        Map<String, String> organizationNumbers, List<String> serviceCodes) {
        List<String> missingSubscriptions = new ArrayList<>();
        for (var entry : organizationNumbers.entrySet()) {
            final var careProviderHsaId = entry.getKey();
            final var careProviderOrgNumber = entry.getValue();
            if (isCareProviderMissingSubscription(careProviderOrgNumber, serviceCodes, restTemplate, httpEntity)) {
                missingSubscriptions.add(careProviderHsaId);
            }
        }
        return missingSubscriptions;
    }

    private boolean isCareProviderMissingSubscription(String careProviderOrgNumber, List<String> serviceCodes, RestTemplate restTemplate,
        HttpEntity<String> httpEntity) {
        for (final var serviceCode : serviceCodes) {
            final var url = kundportalenSubscriptionServiceUrl + "/" + careProviderOrgNumber + "/" + serviceCode;
            final var response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MAP_STRING_BOOLEAN_TYPE);
            if (hasActiveSubscriptionOrServiceCallFailure(response)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasActiveSubscriptionOrServiceCallFailure(ResponseEntity<Map<String, Boolean>> response) {
        if (response.getBody() != null) {
            return !(response.getStatusCode() == HttpStatus.OK && !response.getBody().get("subscriptionActive"));
        }
        return true;
    }

    private List<String> getRelevantServiceCodes(WebCertUser webCertUser) {
        return isPrivatePractitioner(webCertUser) && isElegUser(webCertUser) ? kundportalenElegServiceCodes : kundportalenSithsServiceCodes;
    }

    private String extractOrgNumberFromPersonId(String personId) {
        final var optionalPersonnummer = Personnummer.createPersonnummer(personId);
        return optionalPersonnummer.map(personnummer -> personnummer.getPersonnummerWithDash().substring(2)).orElse("NOT_FOUND");
    }

    private String extractOrgNumberFromCareUnits(List<Vardenhet> careUnits) {
        return careUnits.stream().filter(u -> u.getVardgivareOrgnr() != null).findFirst().map(Vardenhet::getVardgivareOrgnr)
            .orElse("NOT_FOUND");
    }

    private boolean isPrivatePractitioner(WebCertUser webCertUser) {
        return webCertUser.isPrivatLakare();
    }

    private boolean isElegUser(WebCertUser webCertUser) {
        final var authenticationScheme = webCertUser.getAuthenticationScheme();
        return authenticationScheme.equals(FAKE_AUTHENTICATION_ELEG_CONTEXT_REF) || ELEG_AUTHN_CLASSES.contains(authenticationScheme);
    }

    private boolean isFristaendeWebcertUser(String origin) {
        return origin.equals(UserOriginType.NORMAL.name());
    }

    private boolean isPastSubscriptionAdjustmentPeriod(Map<String, Feature> features) {
        return Boolean.TRUE.equals(features.get(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD).getGlobal());
    }

    private boolean isDuringSubscriptionAdjustmentPeriod(Map<String, Feature> features) {
        return Boolean.TRUE.equals(features.get(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD).getGlobal());
    }
}
