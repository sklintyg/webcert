/*
 * Inera Medcert - Sjukintygsapplikation
 *
 * Copyright (C) 2010-2011 Inera AB (http://www.inera.se)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package se.inera.certificate.mc2wc.jpa.model;

import se.vgregion.dao.domain.patterns.entity.AbstractEntity;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Pär Wenåker
 */
@Entity
@Table(name = "CERTIFICATE")
public class Certificate extends AbstractEntity<Certificate, String> {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "CARE_UNIT_ID")
    private String careUnitId;

    @Column(name = "PATIENT_NAME")
    private String patientName;

    @Column(name = "PATIENT_SSN")
    private String patientSsn;

    @Column(name = "SIGNED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signedAt;

    @Column(name = "SENT_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "ORIGIN")
    @Enumerated(EnumType.STRING)
    private CreatorOrigin origin;

    @Column(name = "DOCUMENT")
    @Basic(fetch = FetchType.LAZY)
    @Lob
    private byte[] document;

    @Column(name = "SIGNATURE")
    @Basic(fetch = FetchType.LAZY)
    @Lob
    private byte[] signature;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private State state = State.CREATED;

    @OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy(value = "sentAt desc")
    private Set<Question> questions = new HashSet<Question>();

    public Certificate() {
    }

    /**
     * Constructor. Use {@see CertificateBuilder} to create Certificates.
     *
     * @param id
     * @param fullName
     * @param ssn
     */
    public Certificate(String id, String careUnitId, String patientName, String patientSsn, Date signedAt, CreatorOrigin origin) {
        this.id = id;
        this.careUnitId = careUnitId;
        this.patientName = patientName;
        this.patientSsn = patientSsn;
        this.signedAt = signedAt;
        this.createdAt = new Date();
        this.origin = origin;
    }

    /* (non-Javadoc)
     * @see se.vgregion.dao.domain.patterns.entity.Entity#getId()
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the questions
     */
    public Set<Question> getQuestions() {
        return Collections.unmodifiableSet(questions);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientSsn() {
        return patientSsn;
    }

    public void setPatientSsn(String patientSsn) {
        this.patientSsn = patientSsn;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

    public Date getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Date signedAt) {
        this.signedAt = signedAt;
    }

    public CreatorOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(CreatorOrigin origin) {
        this.origin = origin;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

}
