package se.inera.intyg.webcert.web.service.facade.list.config;

public class TableHeadingDTO {
    private ListColumnTypeDTO id;
    private String title;


    public TableHeadingDTO(ListColumnTypeDTO id, String title) {
        this.id = id;
        this.title = title;
    }

    public ListColumnTypeDTO getId() {
        return id;
    }

    public void setId(ListColumnTypeDTO id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
