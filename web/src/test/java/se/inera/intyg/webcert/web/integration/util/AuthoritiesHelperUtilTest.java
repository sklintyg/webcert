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
package se.inera.intyg.webcert.web.integration.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.db.v1.rest.DbModuleApiV1;
import se.inera.intyg.common.doi.v1.rest.DoiModuleApiV1;
import se.inera.intyg.common.support.common.enumerations.KvIntygstyp;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static se.inera.intyg.webcert.web.integration.util.AuthoritiesHelperUtil.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritiesHelperUtilTest {

    private static final String DB = "db";
    private static final String DOI = "doi";
    private static final String DRAFT = "utkast";
    private static final String CERTIFICATE = "intyg";

    private DoiModuleApiV1 doiModuleApiV1 = new DoiModuleApiV1();
    private DbModuleApiV1 dbModuleApiV1 = new DbModuleApiV1();

    @Test
    public void testCreateDraftAllowed() {
        Assert.assertNull(AuthoritiesHelperUtil.performUniqueAndModuleValidation(new IntygUser(""), DB,
            createEmptyPreviousDraftAndCertificates(), dbModuleApiV1));
    }

    @Test
    public void testDBDraftExistsOnSameCareProviderAndSameUnit() {
        var user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftOnSameCareProviderAndSameUnit();

        Assert.assertEquals(
            String.format(DRAFT_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS, KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDBDraftExistsOnSameCareProviderAndOtherUnit() {
        IntygUser user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftOnSameCareProviderAndOtherUnit();

        Assert.assertEquals(
            String.format(DRAFT_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS, KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDBDraftExistsOnOtherCareProvider() {
        IntygUser user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftOnOtherCareProvider();

        Assert.assertEquals(
            String.format(DRAFT_FROM_OTHER_CARE_PROVIDER_EXISTS, KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDBCertificateExistsOnSameCareProviderAndSameUnit() {
        IntygUser user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftAndCertificateOnSameCareProviderAndSameUnit();

        Assert.assertEquals(
            String.format(CERTIFICATE_FROM_SAME_CARE_PROVIDER_AND_UNIT_EXISTS, KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDBCertificateExistsOnSameCareProviderAndOtherUnit() {
        IntygUser user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftAndCertificateOnSameCareProviderAndOtherUnit();

        Assert.assertEquals(
            String.format(CERTIFICATE_FROM_SAME_CARE_PROVIDER_ON_OTHER_UNIT_EXISTS,
                KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDBCertificateExistsOnOtherCareProvider() {
        IntygUser user = new IntygUser("");
        user.setFeatures(Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG).collect(
            Collectors.toMap(Function.identity(), s -> {
                Feature feature = new Feature();
                feature.setName(s);
                feature.setIntygstyper(Collections.singletonList(DB));
                feature.setGlobal(true);
                return feature;
            })));

        var previousDraftCertificates = createDBDraftAndCertificateOnOtherCareProvider();

        Assert.assertEquals(
            String.format(CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS, KvIntygstyp.getDisplayNameFromCodeValue(DB).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DB, previousDraftCertificates, dbModuleApiV1).getMessage());
    }

    @Test
    public void testDOICertificateExistsOnOtherCareProvider() {
        IntygUser user = new IntygUser("");
        user.setFeatures(
            Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).collect(
                Collectors.toMap(Function.identity(), s -> {
                    Feature feature = new Feature();
                    feature.setName(s);
                    feature.setIntygstyper(Collections.singletonList(DOI));
                    feature.setGlobal(true);
                    return feature;
                })));

        var previousDraftCertificates = createDOICertificateOnOtherCareProvider();

        Assert.assertEquals(
            String.format(CERTIFICATE_FROM_OTHER_CARE_PROVIDER_EXISTS_OVERRIDE, KvIntygstyp.getDisplayNameFromCodeValue(DOI).toLowerCase()),
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DOI, previousDraftCertificates, doiModuleApiV1).getMessage());
    }

    @Test
    public void testDOIDraftButNoDBCertificateExists() {
        IntygUser user = new IntygUser("");
        user.setFeatures(
            Stream.of(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG, AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).collect(
                Collectors.toMap(Function.identity(), s -> {
                    Feature feature = new Feature();
                    feature.setName(s);
                    feature.setIntygstyper(Collections.singletonList(DOI));
                    feature.setGlobal(true);
                    return feature;
                })));

        var previousDraftCertificates = createEmptyPreviousDraftAndCertificates();

        Assert.assertEquals("Det finns inget dödsbevis i nuläget inom vårdgivaren. Dödsorsaksintyget bör alltid skapas efter dödsbeviset.",
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, DOI, previousDraftCertificates, doiModuleApiV1).getMessage());
    }

    private Map<String, Map<String, PreviousIntyg>> createEmptyPreviousDraftAndCertificates() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();
        map.put(DRAFT, new HashMap<>());
        map.put(CERTIFICATE, new HashMap<>());
        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftOnSameCareProviderAndSameUnit() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();
        PreviousIntyg draft = PreviousIntyg.of(true, true, true, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);
        map.put(CERTIFICATE, new HashMap<>());
        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftOnSameCareProviderAndOtherUnit() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();
        PreviousIntyg draft = PreviousIntyg.of(true, false, false, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);
        map.put(CERTIFICATE, new HashMap<>());
        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftOnOtherCareProvider() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();
        PreviousIntyg draft = PreviousIntyg.of(false, false, false, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);
        map.put(CERTIFICATE, new HashMap<>());
        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftAndCertificateOnSameCareProviderAndSameUnit() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();

        PreviousIntyg draft = PreviousIntyg.of(true, true, true, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);

        PreviousIntyg certificate = PreviousIntyg.of(true, true, true, "", "", null);
        Map<String, PreviousIntyg> certificateMap = new HashMap<>();
        certificateMap.put(DB, certificate);
        map.put(CERTIFICATE, certificateMap);

        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftAndCertificateOnSameCareProviderAndOtherUnit() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();

        PreviousIntyg draft = PreviousIntyg.of(true, true, true, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);

        PreviousIntyg certificate = PreviousIntyg.of(true, false, false, "", "", null);
        Map<String, PreviousIntyg> certificateMap = new HashMap<>();
        certificateMap.put(DB, certificate);
        map.put(CERTIFICATE, certificateMap);

        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDBDraftAndCertificateOnOtherCareProvider() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();

        PreviousIntyg draft = PreviousIntyg.of(true, true, true, "", "", null);
        Map<String, PreviousIntyg> draftMap = new HashMap<>();
        draftMap.put(DB, draft);
        map.put(DRAFT, draftMap);

        PreviousIntyg certificate = PreviousIntyg.of(false, false, false, "", "", null);
        Map<String, PreviousIntyg> certificateMap = new HashMap<>();
        certificateMap.put(DB, certificate);
        map.put(CERTIFICATE, certificateMap);

        return map;
    }

    private Map<String, Map<String, PreviousIntyg>> createDOICertificateOnOtherCareProvider() {
        Map<String, Map<String, PreviousIntyg>> map = new HashMap<>();

        map.put(DRAFT, new HashMap<>());

        PreviousIntyg certificate = PreviousIntyg.of(false, false, false, "", "", null);
        Map<String, PreviousIntyg> certificateMap = new HashMap<>();
        certificateMap.put(DOI, certificate);
        map.put(CERTIFICATE, certificateMap);

        return map;
    }

}
