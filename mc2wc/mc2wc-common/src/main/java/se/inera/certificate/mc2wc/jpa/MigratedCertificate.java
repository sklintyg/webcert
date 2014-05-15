package se.inera.certificate.mc2wc.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MIGRATED_CERTIFICATE")
public class MigratedCertificate {

    @Id
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
