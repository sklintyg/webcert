package se.inera.certificate.mc2wc.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "MIGRATED_CERTIFICATE")
public class MigratedCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MIGR_CERT_ID_GEN")
    @TableGenerator(name = "MIGR_CERT_ID_GEN", table = "ID_SEQ_TABLE", pkColumnName = "SEQ_NAME", pkColumnValue = "MIGR_CERT_ID",
            valueColumnName = "SEQ_VALUE", initialValue = 1, allocationSize = 100)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CERTIFICATE_ID")
    private String certificateId;

    @Column(name = "DOCUMENT")
    private byte[] document;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public byte[] getDocument() {
        return document;
    }

    public void setDocument(byte[] document) {
        this.document = document;
    }

}
