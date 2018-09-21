package se.inera.intyg.webcert.persistence.fmb.model.icf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FMB_REFERENS")
public class Referens {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "URI")
    private String uri;

    protected Referens() {
    }

    private Referens(final String text, final String uri) {
        this.text = text;
        this.uri = uri;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUri() {
        return uri;
    }

    public static final class ReferensBuilder {
        private String text;
        private String uri;

        private ReferensBuilder() {
        }

        public static ReferensBuilder aReferens() {
            return new ReferensBuilder();
        }

        public ReferensBuilder text(String text) {
            this.text = text;
            return this;
        }

        public ReferensBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Referens build() {
            return new Referens(text, uri);
        }
    }
}
