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

package se.inera.intyg.webcert.web.integration;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.persistence.privatlakaravtal.model.Avtal;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionerterms.v1.rivtabp21.GetPrivatePractitionerTermsResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsResponseType;
import se.riv.infrastructure.directory.privatepractitioner.getprivatepractitionertermsresponder.v1.GetPrivatePractitionerTermsType;

import se.riv.infrastructure.directory.privatepractitioner.terms.v1.AvtalType;
import se.riv.infrastructure.directory.privatepractitioner.terms.v1.ResultCodeEnum;

/**
 * Created by eriklupander on 2015-08-05.
 */
public class GetPrivatePractitionerTermsResponderImpl implements GetPrivatePractitionerTermsResponderInterface {

    @Autowired
    private AvtalService avtalService;

    @Override
    public GetPrivatePractitionerTermsResponseType getPrivatePractitionerTerms(String logicalAddress, GetPrivatePractitionerTermsType parameters) {

        Avtal latestAvtal = avtalService.getLatestAvtal();
        GetPrivatePractitionerTermsResponseType response = new GetPrivatePractitionerTermsResponseType();

        if (latestAvtal == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("No private practitioner terms found");
            return response;
        }

        AvtalType avtalType = new AvtalType();
        avtalType.setAvtalText(latestAvtal.getAvtalText());
        avtalType.setAvtalVersion(latestAvtal.getAvtalVersion());
        avtalType.setAvtalVersionDatum(latestAvtal.getVersionDatum());
        response.setAvtal(avtalType);
        response.setResultCode(ResultCodeEnum.OK);
        return response;

    }
}
