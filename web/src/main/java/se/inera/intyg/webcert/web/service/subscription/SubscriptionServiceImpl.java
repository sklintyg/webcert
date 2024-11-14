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
package se.inera.intyg.webcert.web.service.subscription;

import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.ELEG;
import static se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum.SITHS;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.integration.api.subscription.AuthenticationMethodEnum;
import se.inera.intyg.webcert.integration.api.subscription.SubscriptionIntegrationService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    private final SubscriptionIntegrationService subscriptionIntegrationService;
    private final MonitoringLogService monitoringLogService;

    public SubscriptionServiceImpl(SubscriptionIntegrationService subscriptionIntegrationService,
        MonitoringLogService monitoringLogService) {
        this.subscriptionIntegrationService = subscriptionIntegrationService;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public boolean checkSubscriptions(WebCertUser webCertUser) {

        if (!isFristaendeWebcertUser(webCertUser)) {
            return true;
        }

        final var careProviderOrgNumbers = getCareProviderOrgNumbers(webCertUser);

        if (!careProviderOrgNumbers.isEmpty()) {
            LOG.debug("Fetching subscription info for WebCertUser with hsaid {}.", webCertUser.getHsaId());
            final var authenticationMethod = isElegUser(webCertUser) ? ELEG : SITHS;
            final var careProviderHsaIds = getMissingSubscriptions(careProviderOrgNumbers, authenticationMethod);

            monitorLogMissingSubscriptions(webCertUser.getHsaId(), authenticationMethod, careProviderHsaIds);
            setSubscriptionActions(webCertUser, careProviderHsaIds);
        }

        return webCertUser.getSubscriptionInfo().getCareProvidersMissingSubscription().isEmpty();
    }

    @Override
    public boolean isUnregisteredElegUserMissingSubscription(String personId) {
        try {
            final var organizationNumber = createOrganizationNumberFromPersonId(personId);
            final var hashedOrganizationNumber = hashed(organizationNumber);
            LOG.debug("Fetching subscription info for unregistered private practitioner with organization number {}.",
                hashedOrganizationNumber);
            final var missingSubscription = isMissingSubscriptionUnregisteredElegUser(organizationNumber);
            monitorLogMissingSubscription(missingSubscription, personId, organizationNumber);
            return missingSubscription;
        } catch (NoSuchElementException e) {
            LOG.error("Failure getting organization number for unregistered eleg user {}.", hash(personId), e);
            return true;
        }
    }

    @Override
    public void acknowledgeSubscriptionModal(WebCertUser webCertUser) {
        final var selectedCareProvider = webCertUser.getValdVardgivare();
        webCertUser.getSubscriptionInfo().getCareProvidersForSubscriptionModal().remove(selectedCareProvider.getId());
    }

    private void setSubscriptionActions(WebCertUser webCertUser, List<String> missingSubscriptions) {
        webCertUser.getSubscriptionInfo().setSubscriptionAction(SubscriptionAction.BLOCK);
        webCertUser.getSubscriptionInfo().setCareProvidersMissingSubscription(List.copyOf(missingSubscriptions));
        webCertUser.getSubscriptionInfo().setCareProvidersForSubscriptionModal(missingSubscriptions);
    }

    private Map<String, List<String>> getCareProviderOrgNumbers(WebCertUser webCertUser) {
        if (isPrivatePractitioner(webCertUser) && isElegUser(webCertUser)) {
            return getOrganizationNumberForElegUser(webCertUser);
        } else {
            final var careProviderOrgNumbers = new HashMap<String, List<String>>();
            for (var careProvider : webCertUser.getVardgivare()) {
                getCareProviderOrganizationNumber(careProvider, careProviderOrgNumbers);
            }
            return careProviderOrgNumbers;
        }
    }

    private Map<String, List<String>> getOrganizationNumberForElegUser(WebCertUser webCertUser) {
        try {
            final var careProvider = webCertUser.getVardgivare().stream().findFirst().map(Vardgivare::getId).orElseThrow();
            final var orgNumber = createOrganizationNumberFromPersonId(webCertUser.getPersonId());
            return Map.of(orgNumber, List.of(careProvider));
        } catch (NoSuchElementException e) {
            LOG.error("Failure getting organization number for private practitioner {}.", hash(webCertUser.getPersonId()), e);
            return Collections.emptyMap();
        }
    }

    private void getCareProviderOrganizationNumber(Vardgivare careProvider, Map<String, List<String>> organizations) {
        try {
            final var organizationNumber = careProvider.getVardenheter().stream().filter(unit -> unit.getVardgivareOrgnr() != null)
                .findFirst().map(Vardenhet::getVardgivareOrgnr).orElseThrow();
            addOrganization(organizationNumber, careProvider.getId(), organizations);
        } catch (NoSuchElementException e) {
            LOG.error("Failure getting organization number for careProvider {}.", careProvider.getId(), e);
        }
    }

    private void addOrganization(String organizationNumber, String careProviderId, Map<String, List<String>> organizations) {
        if (!organizations.containsKey(organizationNumber)) {
            final var careProviderList = new ArrayList<String>();
            careProviderList.add(careProviderId);
            organizations.put(organizationNumber, careProviderList);
        } else {
            final var careProviderIds = organizations.get(organizationNumber);
            if (!careProviderIds.contains(careProviderId)) {
                careProviderIds.add(careProviderId);
            }
        }
    }

    private String createOrganizationNumberFromPersonId(String personId) {
        final var optionalPersonnummer = Personnummer.createPersonnummer(personId);
        return optionalPersonnummer.map(pnr -> pnr.getPersonnummerWithDash().substring(2)).orElseThrow();
    }

    private boolean isPrivatePractitioner(WebCertUser webCertUser) {
        return webCertUser.isPrivatLakare();
    }

    private boolean isElegUser(WebCertUser webCertUser) {
        final var authenticationScheme = webCertUser.getAuthenticationScheme();
        return authenticationScheme.equals(FAKE_AUTHENTICATION_ELEG_CONTEXT_REF) || ELEG_AUTHN_CLASSES.contains(authenticationScheme);
    }

    private boolean isFristaendeWebcertUser(WebCertUser webCertUser) {
        return UserOriginType.NORMAL.name().equals(webCertUser.getOrigin());
    }

    private void monitorLogMissingSubscriptions(String userHsaId, AuthenticationMethodEnum authMethod, List<String> careProviderHsaIds) {
        if (!careProviderHsaIds.isEmpty()) {
            monitoringLogService.logLoginAttemptMissingSubscription(userHsaId, authMethod.name(), careProviderHsaIds.toString());
        }
    }

    private void monitorLogMissingSubscription(boolean missingSubscription, String personId, String organizationNumber) {
        if (missingSubscription) {
            monitoringLogService.logLoginAttemptMissingSubscription(hash(personId), ELEG.name(), hashed(organizationNumber));
        }
    }

    private List<String> getMissingSubscriptions(Map<String, List<String>> careProviderOrgNumbers, AuthenticationMethodEnum authMethod) {
        try {
            return subscriptionIntegrationService.getMissingSubscriptions(careProviderOrgNumbers, authMethod);
        } catch (Exception e) {
            final var careProviderHsaids = flatMapCollection(careProviderOrgNumbers.values());
            LOG.error("Subscription service call failure for care providers {}.", careProviderHsaids, e);
            monitorLogIfServiceCallFailure(careProviderHsaids, e);
            return Collections.emptyList();
        }
    }

    private boolean isMissingSubscriptionUnregisteredElegUser(String organizationNumber) {
        try {
            return subscriptionIntegrationService.isMissingSubscriptionUnregisteredElegUser(organizationNumber);
        } catch (Exception e) {
            LOG.error("Subscription service call failure for unregistered eleg user with org number {}.",
                hashed(organizationNumber), e);
            monitorLogIfServiceCallFailure(List.of(hashed(organizationNumber)), e);
            return false;
        }
    }

    private void monitorLogIfServiceCallFailure(List<String> queryIds, Exception e) {
        if (e instanceof RestClientException) {
            monitoringLogService.logSubscriptionServiceCallFailure(queryIds, e.getMessage());
        }
    }

    private String hash(String personId) {
        return Personnummer.getPersonnummerHashSafe(Personnummer.createPersonnummer(personId).orElse(null));
    }

    private String hashed(String organizationNumber) {
        return HashUtility.hash(organizationNumber);
    }

    private List<String> flatMapCollection(Collection<List<String>> collection) {
        return collection.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
