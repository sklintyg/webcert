package se.inera.intyg.webcert.persistence.roles.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Magnus Ekstrand  on 2015-08-26.
 */
@Entity
@Table(name = "BEFATTNINGSKODER")
public class TitleCode {

    private static final int PRIME = 31;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BEFATTNINGSKOD")
    private String titleCode;

    @Column(name = "GRUPPFORSKRIVARKOD")
    private String groupPrescriptionCode;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false, fetch = FetchType.EAGER)
    @JoinTable(name = "BEFATTNINGSKODER_ROLL", inverseJoinColumns = @JoinColumn(name = "ROLL_ID", referencedColumnName = "ID"), joinColumns = @JoinColumn(name = "BEFATTNINGSKOD_ID", referencedColumnName = "ID"))
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TitleCode titleCode1 = (TitleCode) o;

        if (!groupPrescriptionCode.equals(titleCode1.groupPrescriptionCode)) {
            return false;
        }
        if (!id.equals(titleCode1.id)) {
            return false;
        }
        if (!titleCode.equals(titleCode1.titleCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = PRIME * result + titleCode.hashCode();
        result = PRIME * result + groupPrescriptionCode.hashCode();
        return result;
    }

}
