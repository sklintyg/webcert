/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public class ArendeDraftEntry {
    private String questionId;
    private String intygId;
    private String text;
    private String amne;

    public ArendeDraftEntry() {
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAmne() {
        return amne;
    }

    public void setAmne(String amne) {
        this.amne = amne;
    }

    @JsonIgnore
    public boolean isValid() {
        return intygId != null;
    }

    public static ArendeDraftEntry fromArendeDraft(ArendeDraft arendeDraft) {
        ArendeDraftEntry entry = new ArendeDraftEntry();
        entry.setAmne(arendeDraft.getAmne());
        entry.setText(arendeDraft.getText());
        entry.setQuestionId(arendeDraft.getQuestionId());
        entry.setIntygId(arendeDraft.getIntygId());
        return entry;
    }
}
