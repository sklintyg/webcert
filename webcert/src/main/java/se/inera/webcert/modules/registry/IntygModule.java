package se.inera.webcert.modules.registry;

public class IntygModule implements Comparable<IntygModule> {

    private String id;
    
    private String label;
        
    private String description;
    
    public IntygModule(String id, String label, String description) {
        super();
        this.id = id;
        this.label = label;
        this.description = description;
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
    
    @Override
    public int compareTo(IntygModule o) {
        return getLabel().compareTo(o.getLabel());
    }

}
