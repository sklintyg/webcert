package se.inera.intyg.webcert.web.service.facade.list.config;

public class TableHeadingDTO {
    private ListColumnTypeDTO id;
    private String title;
    private CertificateListItemValueType type;


    public TableHeadingDTO(ListColumnTypeDTO id, String title, CertificateListItemValueType type) {
        this.id = id;
        this.title = title;
        this.type = type;
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

    public CertificateListItemValueType getType() {
        return type;
    }

    public void setType(CertificateListItemValueType type) {
        this.type = type;
    }
}
