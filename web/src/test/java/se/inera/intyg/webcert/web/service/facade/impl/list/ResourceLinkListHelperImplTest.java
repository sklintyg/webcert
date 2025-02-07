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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.list.ResourceLinkListHelperImpl;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItemStatus;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.facade.util.CertificateRelationsConverter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResourceLinkListHelperImplTest {

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;
    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private UserService userService;
    @Mock
    CertificateRelationsConverter certificateRelationsConverter;
    @Mock
    AuthoritiesHelper authoritiesHelper;
    @InjectMocks
    private ResourceLinkListHelperImpl resourceLinkListHelper;

    WebCertUser user = null;

    public void setup(boolean renew, boolean read) {
        final var relations = CertificateRelations
            .builder()
            .children(new CertificateRelation[0])
            .build();

        final var unit = new Vardenhet();
        unit.setId("UNIT_ID");

        user = new WebCertUser();

        when(certificateAccessServiceHelper.isAllowToRenew(any(AccessEvaluationParameters.class))).thenReturn(renew);
        when(certificateAccessServiceHelper.isAllowToRead(any(AccessEvaluationParameters.class))).thenReturn(read);
        when(certificateRelationsConverter.convert(any(Relations.class))).thenReturn(relations);
        when(authoritiesHelper.isFeatureActive(anyString(), anyString())).thenReturn(true);
        when(userService.getLoggedInCareUnit(any(WebCertUser.class))).thenReturn(unit);
        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Nested
    class RenewCertificate {

        @Test
        public void shouldIncludeRenewResourceLinkIfActionLinkExists() {
            setup(true, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.SIGNED.toString(), ActionLinkType.FORNYA_INTYG);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.SIGNED);
            assertEquals(1, resourceLinks.size());
            assertEquals(ResourceLinkTypeDTO.RENEW_CERTIFICATE, resourceLinks.get(0).getType());
        }

        @Test
        public void shouldNotIncludeRenewResourceLinkIfActionLinkDoesNotExist() {
            setup(true, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.SIGNED.toString(), null);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.SIGNED);
            assertEquals(0, resourceLinks.size());
        }

        @Test
        public void shouldNotIncludeRenewResourceLinkIfDraft() {
            setup(true, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(), ActionLinkType.FORNYA_INTYG);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.INCOMPLETE);
            assertEquals(0, resourceLinks.size());
        }

        @Test
        void shouldIncludeRenewResourceLinkIfFromCertificateService() {
            setup(true, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(),
                ActionLinkType.FORNYA_INTYG_FRAN_CERTIFICATE_SERVICE);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.SIGNED);
            assertEquals(1, resourceLinks.size());
            assertEquals(ResourceLinkTypeDTO.RENEW_CERTIFICATE, resourceLinks.get(0).getType());
        }
    }

    @Nested
    class ForwardCertificate {

        @Test
        public void shouldIncludeForwardResourceLinkIfActionLinkExists() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(), ActionLinkType.VIDAREBEFORDRA_UTKAST);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.INCOMPLETE);
            assertEquals(1, resourceLinks.size());
            assertEquals(ResourceLinkTypeDTO.FORWARD_CERTIFICATE, resourceLinks.get(0).getType());
        }

        @Test
        public void shouldNotIncludeForwardResourceLinkIfActionLinkDoesNotExist() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(), null);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.INCOMPLETE);
            assertEquals(0, resourceLinks.size());
        }

        @Test
        public void shouldNotIncludeForwardResourceLinkIfLockedDraft() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.LOCKED.toString(), ActionLinkType.VIDAREBEFORDRA_UTKAST);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.LOCKED);
            assertEquals(0, resourceLinks.size());
        }

        @Test
        public void shouldNotIncludeForwardResourceLinkIfSigned() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.SIGNED.toString(), ActionLinkType.VIDAREBEFORDRA_UTKAST);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.SIGNED);
            assertEquals(0, resourceLinks.size());
        }

        @Test
        public void shouldIncludeForwardResourceLinkIfCompleteDraft() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.COMPLETE.toString(), ActionLinkType.VIDAREBEFORDRA_UTKAST);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.COMPLETE);
            assertEquals(1, resourceLinks.size());
            assertEquals(ResourceLinkTypeDTO.FORWARD_CERTIFICATE, resourceLinks.get(0).getType());
        }

        @Test
        public void shouldNotIncludeForwardResourceLinkIfPrivateDoctor() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.COMPLETE.toString(), ActionLinkType.VIDAREBEFORDRA_UTKAST);
            final var userRoles = new HashMap<String, Role>();
            userRoles.put("PRIVATLAKARE", new Role());
            user.setRoles(userRoles);

            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.COMPLETE);

            assertEquals(0, resourceLinks.size());
        }
    }

    @Nested
    class ReadCertificate {

        @Test
        public void shouldIncludeReadResourceLinkIfActionLinkExists() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(), ActionLinkType.LASA_INTYG);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.INCOMPLETE);
            assertEquals(1, resourceLinks.size());
            assertEquals(ResourceLinkTypeDTO.READ_CERTIFICATE, resourceLinks.get(0).getType());
        }

        @Test
        public void shouldNotIncludeForwardResourceLinkIfActionLinkDoesNotExist() {
            setup(false, false);
            final var entry = setupListIntygEntry(CertificateListItemStatus.INCOMPLETE.toString(), null);
            final var resourceLinks = resourceLinkListHelper.get(entry, CertificateListItemStatus.INCOMPLETE);
            assertEquals(0, resourceLinks.size());
        }
    }

    private ListIntygEntry setupListIntygEntry(String status, ActionLinkType linkType) {
        if (linkType != null) {
            final var link = new ActionLink(linkType);
            return ListTestHelper.createListIntygEntry(status, link);
        } else {
            return ListTestHelper.createListIntygEntry(status, null);
        }
    }
}
