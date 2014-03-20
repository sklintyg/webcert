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

import javax.persistence.*;

/**
 * @author Pär Wenåker
 */
@Entity
@Table(name = "COMPLEMENT")
public class Complement {

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FALT")
    private String falt;

    @Column(name = "TEXT")
    private String text;

    @ManyToOne
    @JoinColumn(nullable = false, name = "QUESTION_ID")
    private Question question;

    @SuppressWarnings("unused")
    private Complement() {
    }

    /**
     * @param falt2
     * @param text2
     */
    public Complement(String falt, String text) {
        this.falt = falt;
        this.text = text;
    }

    public String getFalt() {
        return falt;
    }

    public void setFalt(String falt) {
        this.falt = falt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }


}
