/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.integrationtest.internalapi;

import com.jayway.restassured.RestAssured;
import java.io.FileNotFoundException;
import org.junit.Before;
import org.springframework.http.HttpStatus;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

public abstract class InternalApiBaseRestIntegrationTest extends BaseRestIntegrationTest {

    public static final int OK = HttpStatus.OK.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();

    /**
     * Common setup for all tests
     */
    @Before
    public void setupBase() throws FileNotFoundException {
        super.setupBase();
        RestAssured.baseURI = System.getProperty("integration.tests.actuatorUrl", "http://localhost:9088");
    }

}
