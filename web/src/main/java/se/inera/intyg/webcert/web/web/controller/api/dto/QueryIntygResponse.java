package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.ArrayList;
import java.util.List;

public class QueryIntygResponse {

    private int totalCount = 0;

    private List<ListIntygEntry> results = new ArrayList<>();

    public QueryIntygResponse() {
    }

    public QueryIntygResponse(List<ListIntygEntry> results) {
        this.results = results;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<ListIntygEntry> getResults() {
        return results;
    }

    public void setResults(List<ListIntygEntry> results) {
        this.results = results;
    }

}
