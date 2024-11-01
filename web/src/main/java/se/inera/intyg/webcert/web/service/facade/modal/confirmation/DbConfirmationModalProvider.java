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

package se.inera.intyg.webcert.web.service.facade.modal.confirmation;

import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.metadata.Alert;
import se.inera.intyg.common.support.facade.model.metadata.AlertType;
import se.inera.intyg.common.support.facade.model.metadata.CertificateConfirmationModal;
import se.inera.intyg.common.support.facade.model.metadata.CertificateModalActionType;

@Component
public class DbConfirmationModalProvider implements ConfirmationModalProvider {

    public CertificateConfirmationModal create(String patientName, String patientId, String origin) {
        final var isIntegratedOrigin = origin.equals("DJUPINTEGRATION");

        return CertificateConfirmationModal.builder()
            .title("Kontrollera namn och personnummer på den avlidne")
            .alert(
                Alert.builder()
                    .type(AlertType.ERROR)
                    .text(getAlertText(patientName, patientId))
                    .build()
            )
            .checkboxText("Jag har kontrollerat att personuppgifterna stämmer")
            .primaryAction(CertificateModalActionType.READ)
            .secondaryAction(
                !isIntegratedOrigin ? CertificateModalActionType.CANCEL : CertificateModalActionType.DELETE)
            .text(getText(!isIntegratedOrigin))
            .build();
    }

    private String getText(boolean isNormalOrigin) {
        var text = "Ett dödsbevis utfärdat på fel person får stora konsekvenser för den enskilde personen.\n"
            + "Kontrollera därför en extra gång att personuppgifterna stämmer.";

        if (!isNormalOrigin) {
            text += "\nOm fel personuppgifter visas ovan, välj Radera.";
        }

        return text;
    }

    private String getAlertText(String patientName, String patientId) {
        return "Du är på väg att utfärda ett dödsbevis för\n"
            + patientName
            + " - "
            + patientId;
    }
}
