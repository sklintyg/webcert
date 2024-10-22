/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

/**
 * Testability API for accessing identities from the PP (Privatlakare) stub.
 *
 * @author eriklupander
 */
public class PPStubRestApi {

    @Autowired
    private HoSPersonStub hoSPersonStub;

    @GET
    @Path("/medarbetaruppdrag")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HoSPersonType> getAllHoSPersonType() {
        return hoSPersonStub.getAll();
    }

    @POST
    @Path("/privatepractitioner/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public ValidatePrivatePractitionerResponse validatePrivatePractitioner(String personalIdentityNumber) {
        final var result = new ValidatePrivatePractitionerResponse();
        result.setResultCode(ValidatePrivatePractitionerResultCode.OK);
        return result;
    }

}
