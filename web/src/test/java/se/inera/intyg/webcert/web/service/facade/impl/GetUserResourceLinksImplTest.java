/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetUserResourceLinksImpl;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetUserResourceLinksImplTest {

    @Nested
    class ResourceLinks {

        @InjectMocks
        private GetUserResourceLinksImpl getUserResourceLinks;

        WebCertUser getUserWithOrigin(String origin) {
            final var user = mock(WebCertUser.class);
            when(user.getOrigin()).thenReturn(origin);
            return user;
        }

        WebCertUser getUserWithOriginAndRole(String origin, boolean isDoctor) {
            final var user = mock(WebCertUser.class);
            when(user.getOrigin()).thenReturn(origin);
            when(user.isLakare()).thenReturn(isDoctor);
            return user;
        }

        @Test
        void shallIncludeLogoutIfOriginIsNormal() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallIncludeLogoutIfOriginIsUthopp() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallNotIncludeLogoutIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.LOG_OUT);
        }

        @Test
        void shallIncludeCreateCertificateIfOriginIsNormal() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE);
        }

        @Test
        void shallIncludeCreateCertificateIfOriginIsUthopp() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE);
        }

        @Test
        void shallNotIncludeCreateCertificateIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SEARCH_CREATE_PAGE);
        }

        @Test
        void shallIncludeDraftListIfOriginIsNormal() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_DRAFT_LIST);
        }

        @Test
        void shallIncludeDraftListIfOriginIsUthopp() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_DRAFT_LIST);
        }

        @Test
        void shallNotIncludeDraftListIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_DRAFT_LIST);
        }

        @Test
        void shallIncludeQuestionListIfOriginIsNormal() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_QUESTION_LIST);
        }

        @Test
        void shallIncludeQuestionListIfOriginIsUthopp() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_QUESTION_LIST);
        }

        @Test
        void shallNotIncludeQuestionListIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_QUESTION_LIST);
        }

        @Test
        void shallIncludeSignedCertificatesListIfOriginIsNormal() {
            final var user = getUserWithOriginAndRole("NORMAL", true);
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST);
        }

        @Test
        void shallIncludeSignedCertificatesListIfOriginIsUthopp() {
            final var user = getUserWithOriginAndRole("UTHOPP", true);
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST);
        }

        @Test
        void shallNotIncludeSignedCertificatesListIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST);
        }

        @Test
        void shallNotIncludeSignedCertificatesListIfUserIsNotDoctor() {
            final var user = getUserWithOriginAndRole("NORMAL", false);
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.ACCESS_SIGNED_CERTIFICATES_LIST);
        }

        @Test
        void shallNotIncludeChooseUnitIfOriginIsNormalAndHasLoggedInUnit() {
            final var user = getUserWithOrigin("NORMAL");

            doReturn(getUnit())
                .when(user)
                .getValdVardenhet();

            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.CHOOSE_UNIT);
        }

        @Test
        void shallNotIncludeChooseUnitIfOriginIsDjupintegrationAnHasNoLoggedInUnit() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.CHOOSE_UNIT);
        }

        @Test
        void shallNotIncludeChooseUnitIfOriginIsUthoppAnHasNoLoggedInUnit() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.CHOOSE_UNIT);
        }

        @Test
        void shallIncludeChooseUnitIfOriginIsNormalAndHasNoLoggedInUnit() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.CHOOSE_UNIT);
        }

        @Test
        void shallNotIncludeChangeUnitIfOriginIsDjupintegration() {
            final var user = getUserWithOrigin("DJUPINTEGRATION");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.CHANGE_UNIT);
        }

        @Test
        void shallIncludeChangeUnitIfOriginIsNormal() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.CHANGE_UNIT);
        }

        @Test
        void shallIncludeChangeUnitIfOriginIsUthopp() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.CHANGE_UNIT);
        }

        @Test
        void shallIncludePrivatePractitionerPortalIfUserIsPrivatePractitioner() {
            final var user = getUserWithOrigin("NORMAL");
            when(user.isPrivatLakare()).thenReturn(true);
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.PRIVATE_PRACTITIONER_PORTAL);
        }

        @Test
        void shallNotIncludePrivatePractitionerPortalIfUserIsNotPrivatePractitioner() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.PRIVATE_PRACTITIONER_PORTAL);
        }

        @Test
        void shallIncludeNavigateBackButtonIfUserHasNormalOrigin() {
            final var user = getUserWithOrigin("NORMAL");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.NAVIGATE_BACK_BUTTON);
        }

        @Test
        void shallNotIncludeNavigateBackButtonIfUserHasNormalOrigin() {
            final var user = getUserWithOrigin("UTHOPP");
            final var actualLinks = getUserResourceLinks.get(user);
            ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.NAVIGATE_BACK_BUTTON);
        }

        @Nested
        class SubscriptionWarning {

            public final static String CARE_PROVIDER = "CARE_PROVIDER";

            WebCertUser setupUser(UserOriginType userOriginType, String loggedInCareProvider,
                List<String> missingSubscriptions, List<String> subscriptionWarning) {
                final var user = new WebCertUser();
                user.setOrigin(userOriginType.name());

                if (loggedInCareProvider != null) {
                    final var careProvider = new Vardgivare();
                    careProvider.setId(loggedInCareProvider);
                    user.setValdVardgivare(careProvider);
                }

                final var subscriptionInfo = new SubscriptionInfo();
                subscriptionInfo.setRequireSubscriptionStartDate(LocalDate.now().minusDays(1).toString());
                subscriptionInfo.setSubscriptionAction(SubscriptionAction.WARN);
                subscriptionInfo.setCareProvidersMissingSubscription(missingSubscriptions);
                subscriptionInfo.setCareProvidersForSubscriptionModal(subscriptionWarning);
                user.setSubscriptionInfo(subscriptionInfo);

                return user;
            }

            @Test
            void shallIncludeSubscriptionWarningIfUsersLoggedInCareProviderIsMissingSubscription() {
                final var user = setupUser(
                    UserOriginType.NORMAL,
                    CARE_PROVIDER,
                    Collections.singletonList(CARE_PROVIDER),
                    Collections.singletonList(CARE_PROVIDER)
                );

                final var actualLinks = getUserResourceLinks.get(user);
                ResourceLinkFacadeTestHelper.assertInclude(actualLinks, ResourceLinkTypeDTO.SUBSCRIPTION_WARNING);
            }

            @Test
            void shallNotIncludeSubscriptionWarningIfUsersLoggedInCareProviderIsMissingSubscriptionButHasBeenDisplayed() {
                final var user = setupUser(
                    UserOriginType.NORMAL,
                    CARE_PROVIDER,
                    Collections.singletonList(CARE_PROVIDER),
                    Collections.emptyList()
                );

                final var actualLinks = getUserResourceLinks.get(user);
                ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.SUBSCRIPTION_WARNING);
            }

            @Test
            void shallNotIncludeSubscriptionWarningIfUsersMissingCareProvider() {
                final var user = setupUser(
                    UserOriginType.NORMAL,
                    null,
                    Collections.singletonList(CARE_PROVIDER),
                    Collections.singletonList(CARE_PROVIDER)
                );

                final var actualLinks = getUserResourceLinks.get(user);
                ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.SUBSCRIPTION_WARNING);
            }

            @Test
            void shallNotIncludeSubscriptionWarningIfUsersLoggedInCareProviderIsMissingSubscriptionWhenDjupintegration() {
                final var user = setupUser(
                    UserOriginType.DJUPINTEGRATION,
                    CARE_PROVIDER,
                    Collections.singletonList(CARE_PROVIDER),
                    Collections.singletonList(CARE_PROVIDER)
                );

                final var actualLinks = getUserResourceLinks.get(user);
                ResourceLinkFacadeTestHelper.assertExclude(actualLinks, ResourceLinkTypeDTO.SUBSCRIPTION_WARNING);
            }
        }
    }

    private SelectableVardenhet getUnit() {
        final var unit = new Mottagning();
        unit.setId("UNIT_ID");
        unit.setNamn("UNIT_NAME");
        return unit;
    }
}

