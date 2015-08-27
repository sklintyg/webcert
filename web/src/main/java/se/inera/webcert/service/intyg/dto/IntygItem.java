package se.inera.webcert.service.intyg.dto;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import se.inera.certificate.model.Status;

public class IntygItem {

    private String id;

    private String type;

    private LocalDate fromDate;

    private LocalDate tomDate;

    private List<Status> statuses;

    private LocalDateTime signedDate;

    private String signedBy;

    public IntygItem() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getTomDate() {
        return tomDate;
    }

    public void setTomDate(LocalDate tomDate) {
        this.tomDate = tomDate;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> status) {
        this.statuses = status;
    }

    public LocalDateTime getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(LocalDateTime signedDate) {
        this.signedDate = signedDate;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(String signedBy) {
        this.signedBy = signedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntygItem)) return false;

        IntygItem intygItem = (IntygItem) o;

        return id.equals(intygItem.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
