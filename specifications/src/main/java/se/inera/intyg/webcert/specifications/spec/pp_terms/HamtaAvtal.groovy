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

package se.inera.intyg.webcert.specifications.spec.pp_terms

import org.joda.time.LocalDateTime
import se.inera.intyg.webcert.specifications.spec.util.WsClientFixture
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerterms.v1.rivtabp21.GetPrivatePractitionerTermsResponderInterface
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType

/**
 * Created by eriklupander on 2015-08-05.
 */
class HamtaAvtal extends WsClientFixture {

    private def avtalResponder

    String avtalText
    Integer avtalVersion
    LocalDateTime avtalVersionDatum = null

    boolean avtalVersionDatumNotNull = false


    public HamtaAvtal() {
        super()
    }

    public HamtaAvtal(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String serviceUrl = System.getProperty("service.getPrivatePractitionerTermsUrl")
        String url = serviceUrl ? serviceUrl : baseUrl + "services/get-private-practitioner-terms/v1.0"
        avtalResponder = createClient(GetPrivatePractitionerTermsResponderInterface.class, url)
    }

    public String resultat() {

        GetPrivatePractitionerTermsType request = new GetPrivatePractitionerTermsType();

        def response = avtalResponder.getPrivatePractitionerTerms("", request);

        avtalText = response.avtal.avtalText
        avtalVersion = response.avtal.avtalVersion
        avtalVersionDatum = response.avtal.avtalVersionDatum
        avtalVersionDatumNotNull = avtalVersionDatum != null
        response.resultCode
    }

}
