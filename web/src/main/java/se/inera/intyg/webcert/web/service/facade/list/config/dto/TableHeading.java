package se.inera.intyg.webcert.web.service.facade.list.config.dto;

public class TableHeading {
    private ListColumnType id;
    private String title;
    private CertificateListItemValueType type;


    public TableHeading(ListColumnType id, String title, CertificateListItemValueType type) {
        this.id = id;
        this.title = title;
        this.type = type;
    }

    public ListColumnType getId() {
        return id;
    }

    public void setId(ListColumnType id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CertificateListItemValueType getType() {
        return type;
    }

    public void setType(CertificateListItemValueType type) {
        this.type = type;
    }
}
