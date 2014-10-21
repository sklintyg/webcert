package se.inera.webcert.modules;

public class IntygModule implements Comparable<IntygModule> {

    private String id;

    private String label;

    private String description;

    private String cssPath;

    private String scriptPath;

    private String dependencyDefinitionPath;

    public IntygModule(String id, String label, String description, String cssPath, String scriptPath,
            String dependencyDefinitionPath) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.cssPath = cssPath;
        this.scriptPath = scriptPath;
        this.dependencyDefinitionPath = dependencyDefinitionPath;
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

    public String getCssPath() {
        return cssPath;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public String getDependencyDefinitionPath() {
        return dependencyDefinitionPath;
    }

    @Override
    public int compareTo(IntygModule o) {
        return getLabel().compareTo(o.getLabel());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntygModule) {
            IntygModule other = (IntygModule) obj;
            return id.equals(other.id);
        } else {
            return false;
        }
    }
}
