package se.inera.webcert.persistence.roles.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * Created by Magnus Ekstrand  on 2015-08-26.
 */
@Entity
@Table(name = "BEFATTNINGSKODER")
public class TitleCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BEFATTNINGSKOD")
    private String titleCode;

    @Column(name = "GRUPPFORSKRIVARKOD")
    private String groupPrescriptionCode;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Role role;

    public TitleCode() {
        super();
    }

    public TitleCode(String titleCode, String groupPrescriptionCode, Role role) {
        this.titleCode = titleCode;
        this.groupPrescriptionCode = groupPrescriptionCode;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public String getGroupPrescriptionCode() {
        return groupPrescriptionCode;
    }

    public void setGroupPrescriptionCode(String groupPrescriptionCode) {
        this.groupPrescriptionCode = groupPrescriptionCode;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TitleCode titleCode1 = (TitleCode) o;

        if (!groupPrescriptionCode.equals(titleCode1.groupPrescriptionCode)) return false;
        if (!id.equals(titleCode1.id)) return false;
        if (!titleCode.equals(titleCode1.titleCode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + titleCode.hashCode();
        result = 31 * result + groupPrescriptionCode.hashCode();
        return result;
    }

}
