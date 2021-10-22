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

package se.inera.intyg.webcert.web.web.controller.facade.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Answer;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.common.support.facade.model.question.Reminder;

public class QuestionDTO {

    private String id;
    private QuestionType type;
    private String subject;
    private String message;
    private String author;
    private LocalDateTime sent;
    private Complement[] complements;
    private boolean isHandled;
    private boolean isForwarded;
    private Answer answer;
    private CertificateRelation answeredByCertificate;
    private Reminder[] reminders;
    private LocalDateTime lastUpdate;
    private List<ResourceLinkDTO> links;
    private LocalDate lastDateToReply;

    public static QuestionDTO create(Question question, List<ResourceLinkDTO> links) {
        final var questionDTO = new QuestionDTO();
        questionDTO.id = question.getId();
        questionDTO.type = question.getType();
        questionDTO.subject = question.getSubject();
        questionDTO.message = question.getMessage();
        questionDTO.author = question.getAuthor();
        questionDTO.sent = question.getSent();
        questionDTO.complements = question.getComplements();
        questionDTO.isHandled = question.isHandled();
        questionDTO.isForwarded = question.isForwarded();
        questionDTO.answer = question.getAnswer();
        questionDTO.answeredByCertificate = question.getAnsweredByCertificate();
        questionDTO.reminders = question.getReminders();
        questionDTO.lastUpdate = question.getLastUpdate();
        questionDTO.links = links;
        questionDTO.lastDateToReply = question.getLastDateToReply();
        return questionDTO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getSent() {
        return sent;
    }

    public void setSent(LocalDateTime sent) {
        this.sent = sent;
    }

    public Complement[] getComplements() {
        return complements;
    }

    public void setComplements(Complement[] complements) {
        this.complements = complements;
    }

    public boolean isHandled() {
        return isHandled;
    }

    public void setHandled(boolean handled) {
        isHandled = handled;
    }

    public boolean isForwarded() {
        return isForwarded;
    }

    public void setForwarded(boolean forwarded) {
        isForwarded = forwarded;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public CertificateRelation getAnsweredByCertificate() {
        return answeredByCertificate;
    }

    public void setAnsweredByCertificate(CertificateRelation answeredByCertificate) {
        this.answeredByCertificate = answeredByCertificate;
    }

    public Reminder[] getReminders() {
        return reminders;
    }

    public void setReminders(Reminder[] reminders) {
        this.reminders = reminders;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<ResourceLinkDTO> getLinks() {
        return links;
    }

    public void setLinks(List<ResourceLinkDTO> links) {
        this.links = links;
    }

    public LocalDate getLastDateToReply() {
        return lastDateToReply;
    }

    public void setLastDateToReply(LocalDate lastDateToReply) {
        this.lastDateToReply = lastDateToReply;
    }
}
