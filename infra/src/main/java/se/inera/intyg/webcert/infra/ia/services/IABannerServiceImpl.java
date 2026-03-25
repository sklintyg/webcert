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
package se.inera.intyg.webcert.infra.ia.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.infra.driftbannerdto.Application;
import se.inera.intyg.infra.driftbannerdto.Banner;
import se.inera.intyg.infra.integration.ia.cache.IaCacheConfiguration;
import se.inera.intyg.infra.integration.ia.services.IABannerService;

public class IABannerServiceImpl implements IABannerService {

  private static final Logger LOG = LoggerFactory.getLogger(IABannerServiceImpl.class);

  @Autowired
  @Qualifier("iaRestTemplate")
  private RestTemplate restTemplate;

  @Autowired private Cache iaCache;

  @Value("${intygsadmin.url}")
  private String iaUrl;

  @Override
  public List<Banner> getCurrentBanners() {
    List<Banner> banners = queryCache();

    LocalDateTime today = LocalDateTime.now();

    return banners.stream()
        .filter(
            banner ->
                banner.getDisplayFrom().isBefore(today) && banner.getDisplayTo().isAfter(today))
        .collect(Collectors.toList());
  }

  @Override
  public List<Banner> loadBanners(Application application) {
    String url = iaUrl + "/actuator/banner/" + application.toString();

    LOG.debug("Loading banner from {}", url);

    Banner[] banners = restTemplate.getForObject(url, Banner[].class);

    store(banners);

    return Arrays.asList(banners);
  }

  private void store(Banner[] banners) {
    iaCache.put(IaCacheConfiguration.CACHE_KEY, banners);
  }

  private List<Banner> queryCache() {
    Banner[] banners = iaCache.get(IaCacheConfiguration.CACHE_KEY, Banner[].class);

    if (banners == null) {
      return new ArrayList<>();
    }

    return Arrays.asList(banners);
  }
}
