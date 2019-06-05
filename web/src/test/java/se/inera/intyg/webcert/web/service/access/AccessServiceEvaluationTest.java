/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceEvaluationTest {

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private UtkastService utkastService;

    @Mock
    private WebCertUser webCertUser;

    private AccessServiceEvaluation accessServiceEvaluation;

    private String CERTIFICATE_TYPE = "certificateType";
    private String CERTIFICATE_ID = "certificateId";

    @Before
    public void setup() {
        accessServiceEvaluation = AccessServiceEvaluation.create(webCertUserService,
                patientDetailsResolver,
                utkastService);
    }

    @Test
    public void testCheckRelationComplementedByUtkast() {
        final Relations.FrontendRelations frontendRelations = new Relations.FrontendRelations();
        frontendRelations.setComplementedByUtkast(getRelation(RelationKod.KOMPLT));

        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(frontendRelations))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationComplementedByIntyg() {
        final Relations.FrontendRelations frontendRelations = new Relations.FrontendRelations();
        frontendRelations.setComplementedByIntyg(getRelation(RelationKod.KOMPLT));

        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(frontendRelations))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationReplacedByIntyg() {
        final Relations.FrontendRelations frontendRelations = new Relations.FrontendRelations();
        frontendRelations.setReplacedByIntyg(getRelation(RelationKod.ERSATT));

        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(frontendRelations))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationReplacedByUtkast() {
        final Relations.FrontendRelations frontendRelations = new Relations.FrontendRelations();
        frontendRelations.setReplacedByUtkast(getRelation(RelationKod.ERSATT));

        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(frontendRelations))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.AUTHORIZATION_VALIDATION, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationUtkastCopy() {
        final Relations.FrontendRelations frontendRelations = new Relations.FrontendRelations();
        frontendRelations.setUtkastCopy(getRelation(RelationKod.KOPIA));

        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(frontendRelations))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationNull() {
        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(null)
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    @Test
    public void testCheckRelationLatestChildRelationNull() {
        final AccessResult actualAccessResult = accessServiceEvaluation.given(webCertUser, CERTIFICATE_TYPE)
                .checkReplaced(getRelations(null))
                .evaluate();

        assertNotNull(actualAccessResult);
        assertEquals(AccessResultCode.NO_PROBLEM, actualAccessResult.getCode());
    }

    private Relations getRelations(Relations.FrontendRelations latestChildRelations) {
        final Relations relations = new Relations();
        relations.setParent(null);
        relations.setLatestChildRelations(latestChildRelations);
        return relations;
    }

    private WebcertCertificateRelation getRelation(RelationKod relationKod) {
        return new WebcertCertificateRelation(CERTIFICATE_ID, relationKod, null,
                null, false);
    }
}
