package se.inera.intyg.webcert.web.web.controller.internalapi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IntygInfoResponse {

    private String intygId;
    private String intygType;
    private String intygVersion;

    private LocalDateTime draftCreated;
    private LocalDateTime signedDate;
    private LocalDateTime sentToRecipient;

    private String signedByName;
    private String signedByHsaId;
    private String signedByEmail;

    private String careUnitName;
    private String careUnitHsaId;

    private String careGiverName;
    private String careGiverHsaId;

    private int komletteingar;
    private int komletteingarAnswered;

    private int adminQuestionsSent;
    private int adminQuestionsSentAnswered;

    private int adminQuestionsReceived;
    private int adminQuestionsReceivedAnswered;

    private List<IntygInfoHistory> history = new ArrayList<>();

    public String getIntygId() {
        return intygId;
    }

    public void setIntygId(String intygId) {
        this.intygId = intygId;
    }

    public String getIntygType() {
        return intygType;
    }

    public void setIntygType(String intygType) {
        this.intygType = intygType;
    }

    public String getIntygVersion() {
        return intygVersion;
    }

    public void setIntygVersion(String intygVersion) {
        this.intygVersion = intygVersion;
    }

    public LocalDateTime getDraftCreated() {
        return draftCreated;
    }

    public void setDraftCreated(LocalDateTime draftCreated) {
        this.draftCreated = draftCreated;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
    }

    public LocalDateTime getSentToRecipient() {
        return sentToRecipient;
    }

    public void setSentToRecipient(LocalDateTime sentToRecipient) {
        this.sentToRecipient = sentToRecipient;
    }

    public String getSignedByName() {
        return signedByName;
    }

    public void setSignedByName(String signedByName) {
        this.signedByName = signedByName;
    }

    public String getSignedByHsaId() {
        return signedByHsaId;
    }

    public void setSignedByHsaId(String signedByHsaId) {
        this.signedByHsaId = signedByHsaId;
    }

    public String getSignedByEmail() {
        return signedByEmail;
    }

    public void setSignedByEmail(String signedByEmail) {
        this.signedByEmail = signedByEmail;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCareUnitHsaId() {
        return careUnitHsaId;
    }

    public void setCareUnitHsaId(String careUnitHsaId) {
        this.careUnitHsaId = careUnitHsaId;
    }

    public String getCareGiverName() {
        return careGiverName;
    }

    public void setCareGiverName(String careGiverName) {
        this.careGiverName = careGiverName;
    }

    public String getCareGiverHsaId() {
        return careGiverHsaId;
    }

    public void setCareGiverHsaId(String careGiverHsaId) {
        this.careGiverHsaId = careGiverHsaId;
    }

    public int getKomletteingar() {
        return komletteingar;
    }

    public void setKomletteingar(int komletteingar) {
        this.komletteingar = komletteingar;
    }

    public int getKomletteingarAnswered() {
        return komletteingarAnswered;
    }

    public void setKomletteingarAnswered(int komletteingarAnswered) {
        this.komletteingarAnswered = komletteingarAnswered;
    }

    public int getAdminQuestionsSent() {
        return adminQuestionsSent;
    }

    public void setAdminQuestionsSent(int adminQuestionsSent) {
        this.adminQuestionsSent = adminQuestionsSent;
    }

    public int getAdminQuestionsSentAnswered() {
        return adminQuestionsSentAnswered;
    }

    public void setAdminQuestionsSentAnswered(int adminQuestionsSentAnswered) {
        this.adminQuestionsSentAnswered = adminQuestionsSentAnswered;
    }

    public int getAdminQuestionsReceived() {
        return adminQuestionsReceived;
    }

    public void setAdminQuestionsReceived(int adminQuestionsReceived) {
        this.adminQuestionsReceived = adminQuestionsReceived;
    }

    public int getAdminQuestionsReceivedAnswered() {
        return adminQuestionsReceivedAnswered;
    }

    public void setAdminQuestionsReceivedAnswered(int adminQuestionsReceivedAnswered) {
        this.adminQuestionsReceivedAnswered = adminQuestionsReceivedAnswered;
    }

    public List<IntygInfoHistory> getHistory() {
        return history;
    }
}
