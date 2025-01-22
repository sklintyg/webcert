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
package se.inera.intyg.webcert.persistence.handelse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;

@Embeddable
public class HandelseMetaData {

    @Column(name = "DELIVERY_STATUS", table = "HANDELSE_METADATA")
    @Enumerated(EnumType.STRING)
    private NotificationDeliveryStatusEnum deliveryStatus;

    @Column(name = "CERTIFICATE_TYPE", table = "HANDELSE_METADATA")
    private String certificateType;

    @Column(name = "CERTIFICATE_VERSION", table = "HANDELSE_METADATA")
    private String certificateVersion;

    @Column(name = "CERTIFICATE_ISSUER", table = "HANDELSE_METADATA")
    private String certificateIssuer;


    public HandelseMetaData() {
    }

    public NotificationDeliveryStatusEnum getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(NotificationDeliveryStatusEnum deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getCertificateVersion() {
        return certificateVersion;
    }

    public void setCertificateVersion(String certificateVersion) {
        this.certificateVersion = certificateVersion;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public void setCertificateIssuer(String certificateIssuer) {
        this.certificateIssuer = certificateIssuer;
    }
}
