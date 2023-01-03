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
package se.inera.intyg.webcert.web.service.mail;

public class MailNotification {

    private String qaId;
    private String certificateId;
    private String certificateType;
    private String careUnitId;
    private String careUnitName;
    private String signedByHsaId;

    public MailNotification(String qaId, String certificateId, String certificateType, String careUnitId, String careUnitName,
        String signedByHsaId) {
        this.qaId = qaId;
        this.certificateId = certificateId;
        this.certificateType = certificateType;
        this.careUnitId = careUnitId;
        this.careUnitName = careUnitName;
        this.signedByHsaId = signedByHsaId;
    }

    public String getQaId() {
        return qaId;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public String getSignedByHsaId() {
        return signedByHsaId;
    }
}
