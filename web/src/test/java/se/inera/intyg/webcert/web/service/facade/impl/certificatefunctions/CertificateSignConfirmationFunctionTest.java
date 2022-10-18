/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSignConfirmationFunctionTest {

    @InjectMocks
    private CertificateSignConfirmationFunction certificateSignConfirmationFunction;

    @Test
    void dodsbevisAlreadySignedAnotherCareprovider() {
        final var expectedResourceLink = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
            "Signera och skicka",
            "Intyget skickas direkt till Skatteverket",
            "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare. Det är därför inte möjligt att signera detta dödsbevis.",
            true);

        final var actualResourceLink = certificateSignConfirmationFunction.get(new Certificate());

        assertEquals(expectedResourceLink, actualResourceLink);
    }
}