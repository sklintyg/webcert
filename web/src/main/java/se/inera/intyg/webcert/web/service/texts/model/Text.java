package se.inera.intyg.webcert.web.service.texts.model;

public class Text implements Comparable<Text> {

    private final String id;
    private final String text;
    private final String help;

    public Text(String id, String text, String help) {
        this.id = id;
        this.text = text;
        this.help = help;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getHelp() {
        return help;
    }

    @Override
    public int compareTo(Text o) {
        return text.compareTo(o.getText());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Text other = (Text) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
