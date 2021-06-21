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

import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_REQUIRED;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SubscriptionAction;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.schemas.contract.util.HashUtility;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.subscription.enumerations.AuthenticationMethodEnum;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.subscription.enumerations.SubscriptionState;
import se.inera.intyg.infra.security.common.model.UserOriginType;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    @Value("${require.subscription.start.date}")
    private String requireSubscriptionStartDate;

    private final SubscriptionRestServiceImpl subscriptionRestService;
    private final FeaturesHelper featuresHelper;
    private final MonitoringLogService monitoringLogService;

    public SubscriptionServiceImpl(SubscriptionRestServiceImpl subscriptionRestService,
        FeaturesHelper featuresHelper, MonitoringLogService monitoringLogService) {
        this.subscriptionRestService = subscriptionRestService;
        this.featuresHelper = featuresHelper;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public SubscriptionInfo checkSubscriptions(WebCertUser webCertUser) {
        final var subscriptionState = determineSubscriptionState(webCertUser.getOrigin());
        if (subscriptionState == SubscriptionState.NONE) {
            return new SubscriptionInfo(subscriptionState, requireSubscriptionStartDate);
        }
        LOG.debug("Fetching subscription info for WebCertUser with hsaid {}.", webCertUser.getHsaId());
        final var careProviderOrgNumbers = getCareProviderOrgNumbers(webCertUser);
        final var authenticationMethod = isElegUser(webCertUser) ? AuthenticationMethodEnum.ELEG : AuthenticationMethodEnum.SITHS;
        final var careProviderHsaIds = subscriptionRestService.getMissingSubscriptions(careProviderOrgNumbers);

        monitorLogMissingSubscriptions(webCertUser.getHsaId(), authenticationMethod, careProviderHsaIds);
        blockUsersWithoutAnySubscription(webCertUser, careProviderOrgNumbers.values(), careProviderHsaIds);
        setSubscriptionActions(webCertUser.getVardgivare(), careProviderHsaIds);

        return new SubscriptionInfo(subscriptionState, requireSubscriptionStartDate);
    }

    @Override
    public boolean checkSubscriptionUnregisteredElegUser(String personId) {
        final var organizationNumber = extractOrganizationNumberFromPersonId(personId);
        LOG.debug("Fetching subscription info for unregistered private practitioner with organizion number {}.",
            HashUtility.hash(organizationNumber));
        final var missingSubscription = subscriptionRestService.isMissingSubscriptionUnregisteredElegUser(organizationNumber);
        monitorLogMissingSubscription(missingSubscription, personId, organizationNumber);
        return missingSubscription;
    }

    @Override
    public void acknowledgeSubscriptionWarning(WebCertUser webCertUser) {
        final var selectedCareProvider = (Vardgivare) webCertUser.getValdVardgivare();
        selectedCareProvider.setSubscriptionAction(SubscriptionAction.NONE);

        webCertUser.getVardgivare().stream().filter(cp -> cp.getId().equals(selectedCareProvider.getId())).findFirst()
            .ifPresent(careProvider -> careProvider.setSubscriptionAction(SubscriptionAction.NONE));
    }

    private void setSubscriptionActions(List<Vardgivare> careProviders, List<String> missingSubscriptions) {
        final var action = isSubscriptionRequired() ? SubscriptionAction.BLOCK : SubscriptionAction.WARN;
        Supplier<Stream<Vardgivare>> careProvidersMissing = () -> careProviders.stream().filter(cp -> missingSubscriptions
            .contains(cp.getId()));
        careProvidersMissing.get().forEach(cp -> cp.setSubscriptionAction(action));

        if (action == SubscriptionAction.BLOCK) {
            Supplier<Stream<Vardenhet>> careUnits = () -> careProvidersMissing.get().map(Vardgivare::getVardenheter)
                .flatMap(Collection::stream);
            careUnits.get().forEach(cu -> cu.setSubscriptionAction(action));
            careUnits.get().map(Vardenhet::getMottagningar).flatMap(Collection::stream).forEach(u -> u.setSubscriptionAction(action));
        }
    }

    private void blockUsersWithoutAnySubscription(WebCertUser webCertUser, Collection<String> allCareProviders,
        List<String> careProvidersWithoutSubscription) {

        if (isSubscriptionRequired()
            && missingSubscriptionOnAllCareProviders(allCareProviders, careProvidersWithoutSubscription)) {
            throw new MissingSubscriptionException(String.format("All care providers for user %s are missing subscription",
                webCertUser.getHsaId()));
        }
    }

    private boolean missingSubscriptionOnAllCareProviders(Collection<String> allCareProviders,
        List<String> careProvidersWithoutSubscription) {
        return allCareProviders.size() == careProvidersWithoutSubscription.size()
            && allCareProviders.containsAll(careProvidersWithoutSubscription);
    }

    private SubscriptionState determineSubscriptionState(String requestOrigin) {
        if (isFristaendeWebcertUser(requestOrigin)) {
            if (isSubscriptionRequired()) {
                return SubscriptionState.SUBSCRIPTION_REQUIRED;
            } else if (isSubscriptionAdaptation()) {
                return SubscriptionState.SUBSCRIPTION_ADAPTATION;
            }
        }
        return SubscriptionState.NONE;
    }

    private Map<String, String> getCareProviderOrgNumbers(WebCertUser webCertUser) {
        if (isPrivatePractitioner(webCertUser) && isElegUser(webCertUser)) {
            final var careProvider = webCertUser.getVardgivare().stream().findFirst().map(Vardgivare::getId)
                .orElse("CARE PROVIDER HSA ID NOT_FOUND");
            final var orgNumber = extractOrganizationNumberFromPersonId(webCertUser.getPersonId());
            return Map.of(orgNumber, careProvider);
        } else {
            final var careProviderOrgNumbers = new HashMap<String, String>();
            for (var careProvider : webCertUser.getVardgivare()) {
                final var orgNumber = extractCareProviderOrganizationNumbers(careProvider.getVardenheter());
                careProviderOrgNumbers.put(orgNumber, careProvider.getId());
            }
            return careProviderOrgNumbers;
        }
    }

    private String extractOrganizationNumberFromPersonId(String personId) {
        final var optionalPersonnummer = Personnummer.createPersonnummer(personId);
        return optionalPersonnummer.map(pnr -> pnr.getPersonnummerWithDash().substring(2)).orElse("PERSONUMMER_NOT_FOUND");
    }

    private String extractCareProviderOrganizationNumbers(List<Vardenhet> careUnits) {
        return careUnits.stream().filter(u -> u.getVardgivareOrgnr() != null).findFirst().map(Vardenhet::getVardgivareOrgnr)
            .orElse("ORGANIZATION_NUMBER_NOT_FOUND");
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

    private void monitorLogMissingSubscriptions(String userHsaId, AuthenticationMethodEnum authMethod, List<String> careProviderHsaIds) {
        if (isSubscriptionRequired() && !careProviderHsaIds.isEmpty()) {
            monitoringLogService.logLoginAttemptMissingSubscription(userHsaId, authMethod.name(), careProviderHsaIds.toString());
        }
    }

    private void monitorLogMissingSubscription(boolean missingSubscription, String personId, String organizationNumber) {
        if (isSubscriptionRequired() && missingSubscription) {
            final var optionalPersonId = Personnummer.createPersonnummer(personId);
            final var personIdHash = optionalPersonId.map(Personnummer::getPersonnummerHash).orElse(null);
            monitoringLogService.logLoginAttemptMissingSubscription(personIdHash, AuthenticationMethodEnum.ELEG.name(),
                HashUtility.hash(organizationNumber));
        }
    }

    @Override
    public boolean isSubscriptionRequired() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_REQUIRED);
    }

    @Override
    public boolean isSubscriptionAdaptation() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD)
            && !featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_REQUIRED);
    }

    @Override
    public boolean isAnySubscriptionFeatureActive() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_REQUIRED)
            || featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_ADAPTATION_PERIOD);
    }
}
