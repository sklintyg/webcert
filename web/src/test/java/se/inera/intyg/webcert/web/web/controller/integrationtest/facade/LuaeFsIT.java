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
import se.inera.intyg.common.luae_fs.support.LuaefsEntryPoint;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonDraftIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.CommonLockedCertificateIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.question.AdministrativeQuestionIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.question.ComplementQuestionIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.renew.RenewIT;
import se.inera.intyg.webcert.web.web.controller.integrationtest.facade.testfixture.send.SendIT;

public class LuaeFsIT {

    private static final String CURRENT_VERSION = "1.0";

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeCommonCertificateIT extends CommonCertificateIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of(CURRENT_VERSION);
        }
    }

    @Nested
    class IncludeCommonDraftIT extends CommonDraftIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
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
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of(CURRENT_VERSION);
        }

    }

    @Nested
    class IncludeSendIT extends SendIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeComplementQuestionIT extends ComplementQuestionIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of(CURRENT_VERSION);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    class IncludeRenewIT extends RenewIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }

        @Override
        protected List<String> typeVersionList() {
            return List.of(CURRENT_VERSION);
        }

    }

    @Nested
    class IncludeAdministrativeQuestionIT extends AdministrativeQuestionIT {

        @Override
        protected String moduleId() {
            return LuaefsEntryPoint.MODULE_ID;
        }

        @Override
        protected String typeVersion() {
            return CURRENT_VERSION;
        }
    }
}
