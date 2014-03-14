package se.inera.webcert.modules.registry;

public class IntygModule implements Comparable<IntygModule> {

    private String id;
    
    private String label;
        
    private String description;
    
    public IntygModule(String id, String label) {
        super();
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
        
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }
    
    @Override
    public int compareTo(IntygModule o) {
        return getLabel().compareTo(o.getLabel());
    }

}
