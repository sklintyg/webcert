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

import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.ELEG_AUTHN_CLASSES;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.web.controller.integration.dto.SubscriptionAction;
import se.inera.intyg.infra.security.common.model.UserOriginType;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    @Value("${require.subscription.start.date}")
    private String requireSubscriptionStartDate;

    private final SubscriptionRestServiceImpl subscriptionRestService;

    private final FeaturesHelper featuresHelper;

    public SubscriptionServiceImpl(SubscriptionRestServiceImpl subscriptionRestService,
        FeaturesHelper featuresHelper) {
        this.subscriptionRestService = subscriptionRestService;
        this.featuresHelper = featuresHelper;
    }

    @Override
    public SubscriptionInfo fetchSubscriptionInfo(WebCertUser webCertUser) {
        final var missingSubscriptionAction = determineSubscriptionAction(webCertUser.getOrigin());
        if (missingSubscriptionAction != SubscriptionAction.NONE) {
            LOG.debug("Fetching subscription info for WebCertUser with hsaid {}.", webCertUser.getHsaId());
            final var careProviderOrgNumbers = getCareProviderOrgNumbers(webCertUser);
            final var careProviderHsaIds = subscriptionRestService.getMissingSubscriptions(careProviderOrgNumbers);
            final var authenticationMethod = isElegUser(webCertUser) ? AuthenticationMethodEnum.ELEG : AuthenticationMethodEnum.SITHS;
            final var subscriptionInfo = new SubscriptionInfo(missingSubscriptionAction, careProviderHsaIds, authenticationMethod,
                requireSubscriptionStartDate);

            blockUsersWithoutSubscription(webCertUser, careProviderOrgNumbers.values(), subscriptionInfo.getUnitHsaIdList());

            return subscriptionInfo;

        }
        return SubscriptionInfo.createSubscriptionInfoNoAction();
    }

    @Override
    public boolean fetchSubscriptionInfoUnregisteredElegUser(String personId) {
        final var organizationNumber = extractOrganizationNumberFromPersonId(personId);
        LOG.debug("Fetching subscription info for unregistered private practitioner with organizion number {}.", organizationNumber);
        return subscriptionRestService.isUnregisteredElegUserMissingSubscription(organizationNumber);
    }

    @Override
    public List<String> setAcknowledgedWarning(WebCertUser webCertUser, String hsaId) {
        final var acknowledgedWarnings = webCertUser.getSubscriptionInfo().getAcknowledgedWarnings();
        if (!acknowledgedWarnings.contains(hsaId)) {
            acknowledgedWarnings.add(hsaId);
        }
        return acknowledgedWarnings;
    }

    private void blockUsersWithoutSubscription(WebCertUser webCertUser, Collection<String> allCareProviders,
        List<String> careProvidersWithoutSubscription) {

        if (isPastSubscriptionAdjustmentPeriod()
            && missingSubscriptionOnAllCareProviders(allCareProviders, careProvidersWithoutSubscription)) {
            // TODO Add monitorlog for login attempt without subscription.
            throw new MissingSubscriptionException(String.format("All care providers for user %s are missing subscription",
                webCertUser.getHsaId()));
        }
    }

    private boolean missingSubscriptionOnAllCareProviders(Collection<String> allCareProviders,
        List<String> careProvidersWithoutSubscription) {
        return allCareProviders.size() == careProvidersWithoutSubscription.size()
            && allCareProviders.containsAll(careProvidersWithoutSubscription);
    }

    private SubscriptionAction determineSubscriptionAction(String requestOrigin) {
        if (isFristaendeWebcertUser(requestOrigin)) {
            if (isPastSubscriptionAdjustmentPeriod()) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_BLOCK;
            } else if (isDuringSubscriptionAdjustmentPeriod()) {
                return SubscriptionAction.MISSING_SUBSCRIPTION_WARN;
            }
        }
        return SubscriptionAction.NONE;
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

    @Override
    public boolean isPastSubscriptionAdjustmentPeriod() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD);
    }

    @Override
    public boolean isDuringSubscriptionAdjustmentPeriod() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD)
            && !featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD);
    }

    @Override
    public boolean isAnySubscriptionFeatureActive() {
        return featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD)
            || featuresHelper.isFeatureActive(FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD);
    }
}
