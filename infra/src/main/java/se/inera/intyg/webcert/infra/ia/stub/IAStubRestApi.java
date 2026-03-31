/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.ia.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.driftbannerdto.Banner;
import se.inera.intyg.webcert.infra.ia.cache.IaCacheConfiguration;

@RestController
@Profile("dev")
@RequestMapping("/api/ia-api")
public class IAStubRestApi {

  @Autowired private Cache iaCache;

  @GetMapping("")
  public List<Banner> getBanners() {
    return queryCache();
  }

  @PutMapping("/banner")
  public ResponseEntity<Void> addBanner(@RequestBody Banner banner) {
    banner.setId(UUID.randomUUID());
    List<Banner> banners = queryCache();
    banners.add(banner);
    store(banners.toArray(new Banner[0]));
    return ResponseEntity.ok().build();
  }

  /** Use to evict all clear all banners. */
  @DeleteMapping("/cache")
  public ResponseEntity<String> clearCache() {
    try {
      iaCache.clear();
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  private void store(Banner[] banners) {
    iaCache.put(IaCacheConfiguration.CACHE_KEY, banners);
  }

  private List<Banner> queryCache() {
    Banner[] banners = iaCache.get(IaCacheConfiguration.CACHE_KEY, Banner[].class);
    if (banners == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(banners));
  }
}
