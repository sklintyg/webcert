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
package se.inera.intyg.webcert.web.web.controller.integrationtest.facade;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonDraftIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonLockedCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.candidate.CandidateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.renew.RenewIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.textversion.UpdateToLatestMinorVersionIT;

public class Ag7804IT {

    private static final String CURRENT_VERSION = "1.2";
    private static final String MAJOR_VERSION = "1";

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeCommonCertificateIT extends CommonCertificateIT {


        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2");
        }
    }

    @Nested
    class IncludeCommonDraftIT extends CommonDraftIT {

        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeCommonLockedCertificateIT extends CommonLockedCertificateIT {

        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2");
        }

    }

    @Nested
    class IncludeCandidateIT extends CandidateIT {

        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }

        @Override
        protected String candidateModuleId() {
            return LisjpEntryPoint.MODULE_ID;
        }

        @Override
        protected String candidateTypeVersion() {
            return "1.3";
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeRenewIT extends RenewIT {

        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of("1.0", "1.1", "1.2");
        }
        
    }

    @Nested
    class IncludeUpdateToLatestTextVersionIT extends UpdateToLatestMinorVersionIT {

        @Override
        protected String lastMajorVersion() {
            return MAJOR_VERSION;
        }

        @Override
        protected String moduleId() {
            return Ag7804EntryPoint.MODULE_ID;
        }
    }
}
