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
package se.inera.intyg.webcert.integration.pp.stub;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.ResultCodeEnum;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitioner.v1.rivtabp21.ValidatePrivatePractitionerResponderInterface;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerResponseType;
import se.riv.infrastructure.directory.privatepractitioner.validateprivatepractitionerresponder.v1.ValidatePrivatePractitionerType;

import java.util.ArrayList;
import java.util.List;

// CHECKSTYLE:OFF LineLength
// CHECKSTYLE:ON LineLength

/**
 * Created by Erik Lupander 13/08/15.
 */
public class ValidatePrivatePractitionerResponderStub implements ValidatePrivatePractitionerResponderInterface {

    @Autowired
    private HoSPersonStub personStub;

    @Override
    public ValidatePrivatePractitionerResponseType validatePrivatePractitioner(
            String logicalAddress,
            ValidatePrivatePractitionerType parameters) {

        // Do validation of parameters object
        validate(parameters);

        String id = parameters.getPersonalIdentityNumber();
        Personnummer personnummer = Personnummer.createPersonnummer(id).orElse(null);
        ValidatePrivatePractitionerResponseType response = new ValidatePrivatePractitionerResponseType();
        HoSPersonType person = personStub.get(id);

        if (person == null) {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("No private practitioner with personal identity number: "
                    + Personnummer.getPersonnummerHashSafe(personnummer) + " exists.");
        } else if (person.isGodkandAnvandare()) {
            response.setResultCode(ResultCodeEnum.OK);
        } else {
            response.setResultCode(ResultCodeEnum.ERROR);
            response.setResultText("Private practitioner with personal identity number: "
                    + Personnummer.getPersonnummerHashSafe(personnummer) + " is not authorized to use webcert.");
        }
        return response;
    }

    private void validate(ValidatePrivatePractitionerType parameters) {
        List<String> messages = new ArrayList<>();

        if (parameters == null) {
            messages.add("ValidatePrivatePractitionerType cannot be null.");
        } else {
            String hsaId = parameters.getPersonHsaId();
            String personId = parameters.getPersonalIdentityNumber();

            // Exakt ett av fälten hsaIdentityNumber och personalIdentityNumber ska anges.
            if (Strings.isNullOrEmpty(hsaId) && Strings.isNullOrEmpty(personId)) {
                messages.add("Inget av argumenten hsaId och personId är satt. Ett av dem måste ha ett värde.");
            }

            if (!Strings.isNullOrEmpty(hsaId) && !Strings.isNullOrEmpty(personId)) {
                messages.add("Endast ett av argumenten hsaId och personId får vara satt.");
            }
        }

        if (messages.size() > 0) {
            throw new IllegalArgumentException(Joiner.on(",").join(messages));
        }
    }
}
