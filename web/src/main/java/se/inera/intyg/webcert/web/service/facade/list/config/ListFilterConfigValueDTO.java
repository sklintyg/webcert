package se.inera.intyg.webcert.web.service.facade.list.config;

public class ListFilterConfigValueDTO {
    private String id;
    private String name;
    private boolean defaultValue;

    public static ListFilterConfigValueDTO create(String id, String name, boolean defaultValue) {
        final var value = new ListFilterConfigValueDTO();
        value.setId(id);
        value.setName(name);
        value.setDefaultValue(defaultValue);
        return value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}
