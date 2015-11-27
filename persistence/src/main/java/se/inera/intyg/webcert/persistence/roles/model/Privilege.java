package se.inera.intyg.webcert.persistence.roles.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Magnus Ekstrand on 2015-08-26.
 */
@Entity
@Table(name = "RATTIGHETER")
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAMN")
    private String name;

    @Column(name = "TEXT")
    private String text;

    public Privilege() {
        super();
    }

    public Privilege(final String name) {
        this(name, "");
    }

    public Privilege(final String name, final String text) {
        this.name = name;
        this.text = text;
    }

    //

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Privilege)) {
            return false;
        }

        Privilege privilege = (Privilege) o;

        return name.equals(privilege.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Privilege [name=").append(name).append("]").append("[id=").append(id).append("]");
        return builder.toString();
    }

}
