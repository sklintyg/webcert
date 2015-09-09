package se.inera.webcert.persistence.roles.model;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Collection;
import java.util.HashSet;


/**
 * Created by Magnus Ekstrand  on 2015-08-26.
 */
@Entity
@Table(name = "ROLLER")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAMN")
    private String name;

    @Column(name = "TEXT")
    private String text;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ROLLER_RATTIGHETER", joinColumns = @JoinColumn(name = "ROLL_ID", referencedColumnName = "ID") , inverseJoinColumns = @JoinColumn(name = "RATTIGHET_ID", referencedColumnName = "ID"))
    private Collection<Privilege> privileges = new HashSet<>();

    public Role() {
        super();
    }

    public Role(final String name) {
        this(name, "");
    }

    public Role(final String name, final String text) {
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

    public Collection<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(final Collection<Privilege> privileges) {
        this.privileges = privileges;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Role role = (Role) obj;
        if (!role.equals(role.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Role [name=").append(name).append("]").append("[id=").append(id).append("]");
        return builder.toString();
    }
}
