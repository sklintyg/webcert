package se.inera.certificate.mc2wc.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

@Entity
@Table(name = "MIGRATION_MANIFEST")
public class MigrationManifest {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MANIFEST_ID_GEN")
    @TableGenerator(name = "MANIFEST_ID_GEN", table = "ID_SEQ_TABLE", pkColumnName = "SEQ_NAME", pkColumnValue = "MANIFEST_ID",
            valueColumnName = "SEQ_VALUE", initialValue = 1, allocationSize = 10)
    @Column(name = "MANIFEST_ID")
    private Long manifestId;

    @Column(name = "EXPORTER")
    private String exporter;

    @Column(name = "EXPORT_DATETIME")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime exportDateTime = LocalDateTime.now();

    @Column(name = "CERTIFICATES_FULL")
    private Long certificatesWithContents;

    @Column(name = "CERTIFICATES_EMPTY")
    private Long certificatesWithoutContents;

    @Column(name = "QUESTIONS")
    private Long questions;

    @Column(name = "ANSWERS")
    private Long answers;

    public MigrationManifest(String exporter) {
        super();
        this.exporter = exporter;
    }

    public Long getManifestId() {
        return manifestId;
    }

    public void setManifestId(Long manifestId) {
        this.manifestId = manifestId;
    }

    public String getExporter() {
        return exporter;
    }

    public void setExporter(String exporter) {
        this.exporter = exporter;
    }

    public LocalDateTime getExportDateTime() {
        return exportDateTime;
    }

    public void setExportDateTime(LocalDateTime exportDateTime) {
        this.exportDateTime = exportDateTime;
    }

    public Long getCertificatesWithContents() {
        return certificatesWithContents;
    }

    public void setCertificatesWithContents(Long certificatesWithContents) {
        this.certificatesWithContents = certificatesWithContents;
    }

    public Long getCertificatesWithoutContents() {
        return certificatesWithoutContents;
    }

    public void setCertificatesWithoutContents(Long certificatesWithoutContents) {
        this.certificatesWithoutContents = certificatesWithoutContents;
    }

    public Long getQuestions() {
        return questions;
    }

    public void setQuestions(Long questions) {
        this.questions = questions;
    }

    public Long getAnswers() {
        return answers;
    }

    public void setAnswers(Long answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "MigrationManifest [exporter=" + exporter
                + ", certificatesWithContents=" + certificatesWithContents
                + ", certificatesWithoutContents="
                + certificatesWithoutContents + ", questions=" + questions
                + ", answers=" + answers + "]";
    }

}
