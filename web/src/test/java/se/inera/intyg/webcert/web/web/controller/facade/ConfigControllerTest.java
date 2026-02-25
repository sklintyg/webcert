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
package se.inera.intyg.webcert.web.web.controller.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.infra.driftbannerdto.Application;
import se.inera.intyg.infra.driftbannerdto.Banner;
import se.inera.intyg.infra.dynamiclink.model.DynamicLink;
import se.inera.intyg.infra.dynamiclink.service.DynamicLinkService;
import se.inera.intyg.infra.integration.ia.services.IABannerService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ConfigurationDTO;

@ExtendWith(MockitoExtension.class)
public class ConfigControllerTest {

    @Mock
    private DynamicLinkService dynamicLinkService;

    @Mock
    private IABannerService iaBannerService;

    @InjectMocks
    private ConfigController configController;

    @Nested
    class ConfigControllerTests {

        @Test
        void getConfigurationReturnsVersion() {
            final String version = "1.0";
            ReflectionTestUtils.setField(configController, "version", version);
            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(version, response.getVersion());
        }

        @Test
        void getDynamicLinksReturnsLinks() {
            final var links = Map.of("Test", new DynamicLink());

            doReturn(links)
                .when(dynamicLinkService)
                .getAllAsMap();

            final var response = configController.getDynamicLinks();
            assertTrue(response.containsKey("Test"));
        }

        @Test
        void getConfigurationReturnsBanners() {
            final var banner = new Banner();
            banner.setApplication(Application.WEBCERT);
            final var banners = List.of(banner);

            doReturn(banners)
                .when(iaBannerService)
                .getCurrentBanners();

            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(1, response.getBanners().size());
        }

        @Test
        void getConfigurationReturnsOnlyWCBanners() {
            final var banner = new Banner();
            banner.setApplication(Application.INTYGSSTATISTIK);
            final var banners = List.of(banner);

            doReturn(banners)
                .when(iaBannerService)
                .getCurrentBanners();

            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(0, response.getBanners().size());
        }

        @Test
        void getConfigurationReturnsPrivatePractitionerHost() {
            final String ppHost = "min_sida";
            ReflectionTestUtils.setField(configController, "ppHost", ppHost);
            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(ppHost, response.getPpHost());
        }

        @Test
        void shouldReturnIdpConnectUrlsAsEmptyListWhenConfigIsNull() {
            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(List.of(), response.getIdpConnectUrls());
        }

        @Test
        void shouldReturnIdpConnectUrlsAsEmptyListWhenConfigIsEmpty() {
            ReflectionTestUtils.setField(configController, "idpConnectUrls", "");
            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(List.of(), response.getIdpConnectUrls());
        }

        @Test
        void shouldReturnIdpConnectUrlsWhenConfigExists() {
            final var expected = List.of("https://idp1.example.com", "https://idp2.example.com");

            ReflectionTestUtils.setField(configController, "idpConnectUrls", "https://idp1.example.com,https://idp2.example.com");
            final var response = (ConfigurationDTO) configController.getConfiguration().getEntity();
            assertEquals(expected, response.getIdpConnectUrls());
        }
    }
}
