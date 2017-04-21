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
