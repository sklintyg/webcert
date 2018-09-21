package se.inera.intyg.webcert.persistence.fmb.model.icf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FMB_TYPFALL")
public class TypFall {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPFALLSMENING", nullable = false)
    private String typfallsMening;

    @Column(name = "MAXIMALSJUKRIVNINGSTID", nullable = false)
    private int maximalSjukrivningstid;

    protected TypFall() {
    }

    private TypFall(final String typfallsMening, final int maximalSjukrivningstid) {
        this.typfallsMening = typfallsMening;
        this.maximalSjukrivningstid = maximalSjukrivningstid;
    }

    public Long getId() {
        return id;
    }

    public String getTypfallsMening() {
        return typfallsMening;
    }

    public int getMaximalSjukrivningstid() {
        return maximalSjukrivningstid;
    }

    public static final class TypFallBuilder {
        private String typfallsMening;
        private int maximalSjukrivningstid;

        private TypFallBuilder() {
        }

        public static TypFallBuilder aTypFall() {
            return new TypFallBuilder();
        }

        public TypFallBuilder typfallsMening(String typfallsMening) {
            this.typfallsMening = typfallsMening;
            return this;
        }

        public TypFallBuilder maximalSjukrivningstid(int maximalSjukrivningstid) {
            this.maximalSjukrivningstid = maximalSjukrivningstid;
            return this;
        }

        public TypFall build() {
            return new TypFall(typfallsMening, maximalSjukrivningstid);
        }
    }
}
