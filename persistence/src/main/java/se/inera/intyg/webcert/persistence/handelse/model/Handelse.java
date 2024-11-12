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
package se.inera.intyg.webcert.persistence.handelse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;

@Entity
@Table(name = "HANDELSE")
@SecondaryTable(name = "HANDELSE_METADATA", pkJoinColumns = @PrimaryKeyJoinColumn(name = "HANDELSE_ID"))
public class Handelse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp;

    @Column(name = "KOD")
    @Enumerated(EnumType.STRING)
    private HandelsekodEnum code;

    @Column(name = "INTYGS_ID")
    private String intygsId;

    @Column(name = "ENHETS_ID")
    private String enhetsId;

    @Column(name = "VARDGIVAR_ID")
    private String vardgivarId;

    @Column(name = "PATIENT_PERSON_ID")
    private String personnummer;

    @Column(name = "SISTA_DATUM_FOR_SVAR")
    private LocalDate sistaDatumForSvar;

    @Column(name = "AMNE")
    @Enumerated(EnumType.STRING)
    private ArendeAmne amne;

    @Column(name = "HANTERAT_AV")
    private String hanteratAv;

    @Embedded
    private HandelseMetaData handelseMetaData = new HandelseMetaData();


    public Handelse() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public HandelsekodEnum getCode() {
        return code;
    }

    public void setCode(HandelsekodEnum code) {
        this.code = code;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public void setVardgivarId(String vardgivarId) {
        this.vardgivarId = vardgivarId;
    }

    public String getPersonnummer() {
        return personnummer;
    }

    public void setPersonnummer(String patientPersonId) {
        this.personnummer = patientPersonId;
    }

    public LocalDate getSistaDatumForSvar() {
        return sistaDatumForSvar;
    }

    public void setSistaDatumForSvar(LocalDate sistaDatumForSvar) {
        this.sistaDatumForSvar = sistaDatumForSvar;
    }

    public ArendeAmne getAmne() {
        return amne;
    }

    public void setAmne(ArendeAmne amne) {
        this.amne = amne;
    }

    public String getHanteratAv() {
        return hanteratAv;
    }

    public void setHanteratAv(String hanteratAv) {
        this.hanteratAv = hanteratAv;
    }

    public HandelseMetaData getHandelseMetaData() {
        return handelseMetaData;
    }

    public void setHandelseMetaData(HandelseMetaData handelseMetaData) {
        this.handelseMetaData = handelseMetaData;
    }

    public NotificationDeliveryStatusEnum getDeliveryStatus() {
        return handelseMetaData.getDeliveryStatus();
    }

    public void setDeliveryStatus(NotificationDeliveryStatusEnum deliveryStatus) {
        this.handelseMetaData.setDeliveryStatus(deliveryStatus);
    }

    public String getCertificateType() {
        return this.handelseMetaData.getCertificateType();
    }

    public void setCertificateType(String certificateType) {
        this.handelseMetaData.setCertificateType(certificateType);
    }

    public String getCertificateVersion() {
        return this.handelseMetaData.getCertificateVersion();
    }

    public void setCertificateVersion(String certificateVersion) {
        this.handelseMetaData.setCertificateVersion(certificateVersion);
    }

    public String getCertificateIssuer() {
        return this.handelseMetaData.getCertificateIssuer();
    }

    public void setCertificateIssuer(String certificateIssuer) {
        this.handelseMetaData.setCertificateIssuer(certificateIssuer);
    }
}
