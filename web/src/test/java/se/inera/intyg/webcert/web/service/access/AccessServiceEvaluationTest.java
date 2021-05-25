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

package se.inera.intyg.webcert.web.service.access;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceEvaluationTest {

    @Mock
    private WebCertUserService webCertUserService;
    @Mock
    private PatientDetailsResolver patientDetailsResolver;
    @Mock
    private UtkastService utkastService;
    @Mock
    private IntygTextsService intygTextsService;
    @Mock
    private WebCertUser user;

    private AccessServiceEvaluation accessServiceEvaluation;

    @Before
    public void setup() {
        accessServiceEvaluation = AccessServiceEvaluation
            .create(webCertUserService, patientDetailsResolver, utkastService, intygTextsService);
    }

    @Test
    public void shallBlockIfNotOfLatestMajorVersion() {
        final var actualAccessResult = accessServiceEvaluation.given(user, "ts-bas").checkLatestCertificateTypeVersion("6.8").evaluate();
        Assert.assertEquals(AccessResultCode.NOT_LATEST_MAJOR_VERSION, actualAccessResult.getCode());
    }
}