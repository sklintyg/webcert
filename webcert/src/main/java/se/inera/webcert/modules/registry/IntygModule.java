package se.inera.webcert.modules.registry;

public class IntygModule implements Comparable<IntygModule> {

    private Integer sortValue = 0;
    
    private String id;
    
    private String label;
    
    private String url;
    
    private String description;
    
    public IntygModule(String id, String label, String url, Integer sortValue) {
        super();
        this.id = id;
        this.label = label;
        this.url = url;
        this.sortValue = sortValue;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public Integer getSortValue() {
        return sortValue;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }
    
    @Override
    public int compareTo(IntygModule o) {
        return getSortValue().compareTo(o.getSortValue());
    }

}
