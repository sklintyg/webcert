/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.modal.confirmation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import se.inera.intyg.common.support.facade.model.config.MessageLevel;
import se.inera.intyg.common.support.facade.model.metadata.Alert;
import se.inera.intyg.common.support.facade.model.metadata.CertificateConfirmationModal;
import se.inera.intyg.common.support.facade.model.metadata.CertificateModalActionType;

class DbSignConfirmationModalProviderTest {

    private static final String NAME = "NAME MIDDLE LAST";
    private static final String PATIENT_ID = "ID";

    private final DbSignConfirmationModalProvider provider = new DbSignConfirmationModalProvider();

    @Test
    void shouldReturnExpectedModalForNormalOrigin() {
        final var expected = CertificateConfirmationModal.builder()
            .title("Kontrollera namn och personnummer på den avlidne")
            .alert(
                Alert.builder()
                    .type(MessageLevel.ERROR)
                    .text(
                        "När dödsbevis signeras, skickas det samtidigt till Skatteverket och dödsfallet registreras. "
                            + "<strong>Detta går inte att ångra.</strong>"
                    )
                    .build()
            )
            .checkboxText("<div>Jag intygar att dödsbevis ska utfärdas för<strong> NAME MIDDLE LAST - ID</strong></div>")
            .primaryAction(CertificateModalActionType.SIGN)
            .secondaryAction(CertificateModalActionType.CANCEL)
            .text("För att kunna signera behöver du kontrollera att personuppgifterna stämmer.")
            .build();

        assertEquals(expected, provider.create(NAME, PATIENT_ID, "NORMAL"));
    }

    @Test
    void shouldReturnExpectedModalForIntegratedOrigin() {
        final var expected = CertificateConfirmationModal.builder()
            .title("Kontrollera namn och personnummer på den avlidne")
            .alert(
                Alert.builder()
                    .type(MessageLevel.ERROR)
                    .text(
                        "När dödsbevis signeras, skickas det samtidigt till Skatteverket och dödsfallet registreras. "
                            + "<strong>Detta går inte att ångra.</strong>"
                    ).build()
            )
            .checkboxText("<div>Jag intygar att dödsbevis ska utfärdas för<strong> NAME MIDDLE LAST - ID</strong></div>")
            .primaryAction(CertificateModalActionType.SIGN)
            .secondaryAction(CertificateModalActionType.CANCEL)
            .text("För att kunna signera behöver du kontrollera att personuppgifterna stämmer.")
            .build();

        assertEquals(expected, provider.create(NAME, PATIENT_ID, "DJUPINTEGRATION"));
    }

}
