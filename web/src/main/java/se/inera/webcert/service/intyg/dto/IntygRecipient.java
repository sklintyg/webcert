package se.inera.webcert.service.intyg.dto;

public class IntygRecipient {

    private String id;

    private String name;

    private String logicalAddress;

    public IntygRecipient(String id, String name, String logicalAddress) {
        super();
        this.id = id;
        this.name = name;
        this.logicalAddress = logicalAddress;
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

    public String getLogicalAddress() {
        return logicalAddress;
    }

    public void setLogicalAddress(String logicalAddress) {
        this.logicalAddress = logicalAddress;
    }

}
