/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Arenden;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.v3.Patient;

public class NotificationRedeliveryDTO {

    private Intyg cert;
    private String certId;
    private String certType;
    private Patient patient;
    private Handelse event;
    private Arenden sent;
    private Arenden received;
    private String reference;
    private HsaId handler;

    public NotificationRedeliveryDTO() { }

    public Intyg getCert() {
        return cert;
    }

    public void setCert(Intyg cert) {
        this.cert = cert;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Handelse getEvent() {
        return event;
    }

    public void setEvent(Handelse event) {
        this.event = event;
    }

    public Arenden getSent() {
        return sent;
    }

    public void setSent(Arenden sent) {
        this.sent = sent;
    }

    public Arenden getReceived() {
        return received;
    }

    public void setReceived(Arenden received) {
        this.received = received;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public HsaId getHandler() {
        return handler;
    }

    public void setHandler(HsaId handler) {
        this.handler = handler;
    }

    public NotificationRedeliveryDTO set(CertificateStatusUpdateForCareType statusUpdate) {
        this.certId = statusUpdate.getIntyg().getIntygsId().getExtension();
        this.certType = statusUpdate.getIntyg().getTyp().getCode();
        this.event = statusUpdate.getHandelse();
        this.sent = statusUpdate.getSkickadeFragor();
        this.received = statusUpdate.getMottagnaFragor();
        this.reference = statusUpdate.getRef();
        this.handler = statusUpdate.getHanteratAv();

        if (!isCertificate(statusUpdate)) {
            this.cert = statusUpdate.getIntyg();
        } else {
            this.patient = statusUpdate.getIntyg().getPatient();
        }
        return this;
    }

    public CertificateStatusUpdateForCareType get() {
        CertificateStatusUpdateForCareType statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setHandelse(this.event);
        statusUpdate.setSkickadeFragor(this.sent);
        statusUpdate.setMottagnaFragor(this.received);
        statusUpdate.setRef(this.reference);
        statusUpdate.setHanteratAv(this.handler);

        if (!this.isCertificate()) {
            statusUpdate.setIntyg(this.cert);
        }
        return statusUpdate;
    }

    public boolean isCertificate() {
        return this.cert == null;
    }

    private boolean isCertificate(CertificateStatusUpdateForCareType statusUpdate) {
        return statusUpdate.getIntyg().getUnderskrift() != null;
    }
}
