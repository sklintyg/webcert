/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.textversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.CommonFacadeITSetup;
import se.inera.intyg.webcert.web.web.controller.testability.facade.dto.CreateCertificateFillType;

public abstract class UpdateToLatestMinorVersionIT extends CommonFacadeITSetup {

    protected abstract String lastMajorVersion();

    protected abstract String moduleId();

    private String latestMinorTextVersion;
    private String previousMinorTextVersion;

    @BeforeEach
    void setup() {
        latestMinorTextVersion = getLatestMinorTextVersion(moduleId(), lastMajorVersion());
        previousMinorTextVersion = getPreviousMinorTextVersion(moduleId(), lastMajorVersion());
    }

    @Test
    @DisplayName("Should get latest text version for draft")
    public void shouldOpenSavedDraftWithLatestTextVersion() {
        final var testSetup = getDraftTestSetupBuilder(CreateCertificateFillType.MINIMAL, moduleId(), previousMinorTextVersion)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var certificateResponse = getCertificate(testSetup);

        assertEquals(latestMinorTextVersion, certificateResponse.getMetadata().getTypeVersion());
    }

    @Test
    @DisplayName("Should get text version for certificate")
    public void shouldOpenSignedCertificateWithOriginalTextVersion() {
        final var testSetup = getCertificateTestSetupBuilder(moduleId(), previousMinorTextVersion)
            .useDjupIntegratedOrigin()
            .setup();

        certificateIdsToCleanAfterTest.add(testSetup.certificateId());

        final var certificateResponse = getCertificate(testSetup);

        assertEquals(previousMinorTextVersion, certificateResponse.getMetadata().getTypeVersion());
    }
}
