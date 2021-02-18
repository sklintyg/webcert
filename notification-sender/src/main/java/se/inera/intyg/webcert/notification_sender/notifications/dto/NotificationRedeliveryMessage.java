/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

public class NotificationRedeliveryMessage implements Serializable {

    private Intyg cert;
    private Patient patient;
    private CertificateMessages sent;
    private CertificateMessages received;
    private String reference;

    public NotificationRedeliveryMessage() { }

    public Intyg getCert() {
        return cert;
    }

    public void setCert(Intyg cert) {
        this.cert = cert;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public CertificateMessages getSent() {
        return sent;
    }

    public void setSent(CertificateMessages sent) {
        this.sent = sent;
    }

    public CertificateMessages getReceived() {
        return received;
    }

    public void setReceived(CertificateMessages received) {
        this.received = received;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonIgnore
    public boolean hasCertificate() {
        return this.cert != null;
    }
}
