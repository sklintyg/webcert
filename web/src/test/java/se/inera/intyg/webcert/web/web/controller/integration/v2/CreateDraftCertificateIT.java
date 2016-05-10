/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integration.v2;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

/**
 * Created by eriklupander on 2016-05-10.
 */
public class CreateDraftCertificateIT {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateIT.class);



    /* Replace later with StringTemplate */
    private static final String REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:riv:itintegration:registry:1\" xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:certificate:CreateDraftCertificateResponder:2\" xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:certificate:types:2\" xmlns:urn3=\"urn:riv:clinicalprocess:healthcond:certificate:2\">\n" +
            "   <soapenv:Header>\n" +
            "      <urn:LogicalAddress></urn:LogicalAddress>\n" +
            "   </soapenv:Header>\n" +
            "   <soapenv:Body>\n" +
            "      <urn1:CreateDraftCertificate>\n" +
            "         <urn1:intyg>\n" +
            "            <urn1:typAvIntyg>\n" +
            "               <urn2:code>luae_fs</urn2:code>\n" +
            "               <urn2:codeSystem>f6fb361a-e31d-48b8-8657-99b63912dd9b</urn2:codeSystem>\n" +
            "               <!--Optional:-->\n" +
            "               <urn2:displayName>Läkarutlåtande Aktivitetsersättning vid Förlängd skolgång</urn2:displayName>\n" +
            "            </urn1:typAvIntyg>\n" +
            "            <urn1:patient>\n" +
            "               <urn3:person-id>\n" +
            "                  <urn2:root>1.2.752.129.2.1.3.1</urn2:root>\n" +
            "                  <urn2:extension>191212121212</urn2:extension>\n" +
            "               </urn3:person-id>\n" +
            "               <urn3:fornamn>Tolvan</urn3:fornamn>\n" +
            "               <urn3:efternamn>Tolvansson</urn3:efternamn>\n" +
            "               <!--Optional:-->\n" +
            "               <urn3:mellannamn></urn3:mellannamn>\n" +
            "               <urn3:postadress>Postvägen 1</urn3:postadress>\n" +
            "               <urn3:postnummer>123 45</urn3:postnummer>\n" +
            "               <urn3:postort>Tolvstad</urn3:postort>\n" +
            "               <!--You may enter ANY elements at this point-->\n" +
            "            </urn1:patient>\n" +
            "            <urn1:skapadAv>\n" +
            "               <urn1:personal-id>\n" +
            "                  <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "                  <urn2:extension>IFV1239877878-1049</urn2:extension>\n" +
            "               </urn1:personal-id>\n" +
            "               <urn1:fullstandigtNamn>Jan Nilsson</urn1:fullstandigtNamn>\n" +
            "               <urn1:enhet>\n" +
            "                  <urn1:enhets-id>\n" +
            "                     <urn2:root>1.2.752.129.2.1.4.1</urn2:root>\n" +
            "                     <urn2:extension>IFV1239877878-1042</urn2:extension>\n" +
            "                  </urn1:enhets-id>\n" +
            "                  <urn1:enhetsnamn>Webcert Enhet 1</urn1:enhetsnamn>\n" +
            "                  <!--You may enter ANY elements at this point-->\n" +
            "               </urn1:enhet>\n" +
            "               <!--You may enter ANY elements at this point-->\n" +
            "            </urn1:skapadAv>\n" +
            "            <!--You may enter ANY elements at this point-->\n" +
            "         </urn1:intyg>\n" +
            "         <!--You may enter ANY elements at this point-->\n" +
            "      </urn1:CreateDraftCertificate>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private static final String BASE = "Envelope.Body.CreateDraftCertificateResponse.";

    @Before
    public void setup() {
        RestAssured.requestSpecification = new RequestSpecBuilder().setContentType("application/xml;charset=utf-8").build();
        RestAssured.baseURI = System.getProperty("integration.tests.baseUrl");
    }

    @Test
    public void testCreateLuaefsDraft() {

        LOG.info("Test executing with baseURI: " + RestAssured.baseURI);

        given().with().body(REQUEST)
                .expect()
                .statusCode(200)
                .body(BASE + "result.resultCode", is("OK"))
                .body(BASE + "intygs-id.extension.size()", is(1)) // Expect one such element.
                .when()
                .post(RestAssured.baseURI + "services/create-draft-certificate/v2.0");
    }
}
