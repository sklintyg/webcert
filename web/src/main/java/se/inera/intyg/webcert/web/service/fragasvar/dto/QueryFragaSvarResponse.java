package se.inera.intyg.webcert.web.service.fragasvar.dto;

import java.util.List;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public class QueryFragaSvarResponse {
    private int totalCount;
    private List<FragaSvar> results;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<FragaSvar> getResults() {
        return results;
    }

    public void setResults(List<FragaSvar> results) {
        this.results = results;
    }
}
