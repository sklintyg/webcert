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
package se.inera.intyg.webcert.infra.security.authorities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.Feature;

@ExtendWith(MockitoExtension.class)
public class FeaturesHelperTest {

  @Mock private CommonFeaturesResolver commonFeaturesResolver;

  @InjectMocks private FeaturesHelper featuresHelper;

  private static final String FEATURE_NAME = "SEKRETESSMARKERING";
  private static final String SEKRETESSMARKERING_TILLATEN = "Sekretessmarkering tillåten";
  private static final List<String> CERTIFICATE_TYPES = ImmutableList.of("type1");

  @Test
  public void testReadActiveFeature() {
    final ImmutableMap<String, Feature> featureMap = createActiveFeatureMap();
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var featureActive = featuresHelper.isFeatureActive(FEATURE_NAME);
    assertTrue(featureActive);
  }

  @Test
  public void shallReturnCertificateTypesForActiveFeature() {
    final var featureMap = createActiveFeatureMap();
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actualCertificateTypes = featuresHelper.getCertificateTypesForFeature(FEATURE_NAME);
    assertEquals(CERTIFICATE_TYPES, actualCertificateTypes);
  }

  @Test
  public void shallReturnEmptyCertificateTypesForInActiveFeature() {
    final var featureMap = createActiveFeatureMap();
    featureMap.values().forEach(feature -> feature.setGlobal(false));
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actualCertificateTypes = featuresHelper.getCertificateTypesForFeature(FEATURE_NAME);
    assertEquals(Collections.emptyList(), actualCertificateTypes);
  }

  @Test
  public void shallReturnEmptyCertificateTypesForActiveFeatureWithoutTypes() {
    final var featureMap = createActiveFeatureMap();
    featureMap.values().forEach(feature -> feature.setIntygstyper(Collections.emptyList()));
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actualCertificateTypes = featuresHelper.getCertificateTypesForFeature(FEATURE_NAME);
    assertEquals(Collections.emptyList(), actualCertificateTypes);
  }

  @Test
  public void shallReturnTrueForActiveFeatureForCertificateType() {
    final var featureMap = createActiveFeatureMap();
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actual = featuresHelper.isFeatureActive(FEATURE_NAME, "type1");
    assertEquals(Boolean.TRUE, actual);
  }

  @Test
  public void shallReturnFalseForActiveFeatureButMissingType() {
    final var featureMap = createActiveFeatureMap();
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actual = featuresHelper.isFeatureActive(FEATURE_NAME, "type2");
    assertEquals(Boolean.FALSE, actual);
  }

  @Test
  public void shallReturnFalseForInActive() {
    final var featureMap = createActiveFeatureMap();
    featureMap.values().forEach(feature -> feature.setGlobal(false));
    doReturn(featureMap).when(commonFeaturesResolver).getFeatures();
    final var actual = featuresHelper.isFeatureActive(FEATURE_NAME, "type1");
    assertEquals(Boolean.FALSE, actual);
  }

  private ImmutableMap<String, Feature> createActiveFeatureMap() {
    final var feature = new Feature();
    feature.setDesc(SEKRETESSMARKERING_TILLATEN);
    feature.setGlobal(true);
    feature.setName(FEATURE_NAME);
    feature.setIntygstyper(CERTIFICATE_TYPES);
    return ImmutableMap.of(FEATURE_NAME, feature);
  }
}
