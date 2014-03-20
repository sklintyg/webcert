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
import java.util.Date;

/**
 * @author Pär Wenåker
 */
@Entity
@Table(name = "ANSWER")
public class Answer extends AbstractEntity<Answer, String> {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "ORIGINATOR")
    private String originator;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private State state = State.EDITED;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED")
    private Date created;

    @Column(name = "TEXT", length = 2048)
    private String text;

    @Column(name = "TEXT_SIGNED_AT")
    private Date textSignedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SENT_AT")
    private Date sentAt;

    @Embedded
    private AddressCare addressCare = new AddressCare();

    // moved in from AnswerFromCare
    @Column(name = "SIGNED_DATA", length = 2048)
    private String signedData;

    // moved in from AnswerFromFk
    @Column(name = "FK_CONTACT")
    private String fkContact;

    @OneToOne
    @JoinColumn(name = "QUESTION_ID")
    private Question question;

    public Answer() {
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

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTextSignedAt() {
        return textSignedAt;
    }

    public void setTextSignedAt(Date textSignedAt) {
        this.textSignedAt = textSignedAt;
    }

    public AddressCare getAddressCare() {
        return addressCare;
    }

    public void setAddressCare(AddressCare addressCare) {
        this.addressCare = addressCare;
    }

    public String getSignedData() {
        return signedData;
    }

    public void setSignedData(String signedData) {
        this.signedData = signedData;
    }

    public String getFkContact() {
        return fkContact;
    }

    public void setFkContact(String fkContact) {
        this.fkContact = fkContact;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
