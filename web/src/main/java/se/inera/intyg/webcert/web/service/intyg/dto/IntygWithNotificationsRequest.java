package se.inera.intyg.webcert.web.service.intyg.dto;

import java.time.LocalDateTime;
import java.util.List;

import se.inera.intyg.schemas.contract.Personnummer;

public final class IntygWithNotificationsRequest {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final List<String> enhetId;
    private final String vardgivarId;
    private final Personnummer personnummer;

    public IntygWithNotificationsRequest(LocalDateTime startDate, LocalDateTime endDate, List<String> enhetId, String vardgivarId,
            Personnummer personnummer) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.enhetId = enhetId;
        this.vardgivarId = vardgivarId;
        this.personnummer = personnummer;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public List<String> getEnhetId() {
        return enhetId;
    }

    public String getVardgivarId() {
        return vardgivarId;
    }

    public Personnummer getPersonnummer() {
        return personnummer;
    }

    public boolean shouldUseEnhetId() {
        return enhetId != null && !enhetId.isEmpty();
    }

    public static class Builder {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<String> enhetId;
        private String vardgivarId;
        private Personnummer personnummer;

        public Builder setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder setEnhetId(List<String> enhetId) {
            this.enhetId = enhetId;
            return this;
        }

        public Builder setVardgivarId(String vardgivarId) {
            this.vardgivarId = vardgivarId;
            return this;
        }

        public Builder setPersonnummer(Personnummer personnummer) {
            this.personnummer = personnummer;
            return this;
        }

        public IntygWithNotificationsRequest build() {
            return new IntygWithNotificationsRequest(startDate, endDate, enhetId, vardgivarId, personnummer);
        }
    }
}
