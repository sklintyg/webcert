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
package se.inera.certificate.mc2wc.medcert.jpa.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Pär Wenåker
 */
@Entity
@Table(name = "QUESTION")
public class Question extends AbstractEntity<Question, String> {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "ORIGINATOR")
    private String originator;

    @Column(name = "FK_REFERENCE_ID")
    private String fkReferenceId;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private State state = State.CREATED;

    @Column(name = "CAPTION")
    private String caption;

    @Column(name = "SUBJECT")
    @Enumerated(EnumType.STRING)
    private Subject subject;

    @Column(name = "TEXT", length = 2048)
    private String text;

    @Column(name = "TEXT_SIGNED_AT")
    private Date textSignedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SENT_AT")
    private Date sentAt;

    @Embedded
    private Patient patient = new Patient();

    @Embedded
    private AddressCare addressCare = new AddressCare();

    @Embedded
    private AddressFk addressFk = new AddressFk();

    @OneToOne(mappedBy = "question", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Answer answer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CERTIFICATE_ID")
    private Certificate certificate;

    // moved in from QuestionFromFk
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "question")
    private Set<Complement> complements = new HashSet<Complement>();

    @Column(name = "LAST_DATE_FOR_ANSWER")
    private Date lastDateForAnswer;

    // moved in from QuestionFromCare
    @Column(name = "SIGNED_DATA", length = 2048)
    private String signedData;

    public Question() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getFkReferenceId() {
        return fkReferenceId;
    }

    public void setFkReferenceId(String fkReferenceId) {
        this.fkReferenceId = fkReferenceId;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public Date getTextSignedAt() {
        return textSignedAt;
    }

    public void setTextSignedAt(Date textSignedAt) {
        this.textSignedAt = textSignedAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public AddressCare getAddressCare() {
        return addressCare;
    }

    public void setAddressCare(AddressCare addressCare) {
        this.addressCare = addressCare;
    }

    public AddressFk getAddressFk() {
        return addressFk;
    }

    public void setAddressFk(AddressFk addressFk) {
        this.addressFk = addressFk;
    }

    public Set<Complement> getComplements() {
        return Collections.unmodifiableSet(complements);
    }

    public Date getLastDateForAnswer() {
        return lastDateForAnswer;
    }

    public void setLastDateForAnswer(Date lastDateForAnswer) {
        this.lastDateForAnswer = lastDateForAnswer;
    }

    public String getSignedData() {
        return signedData;
    }

    public void setSignedData(String signedData) {
        this.signedData = signedData;
    }

    @Override
    public String toString() {
        return "Question [id=" + id + "]";
    }

}
