package se.inera.certificate.mc2wc.medcert.jpa.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class AbstractEntity<T extends Entity<T, ID>, ID> implements Entity<T, ID> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public final boolean sameAs(final T other) {
        return other != null && this.getId().equals(other.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        if (getId() == null) {
            return super.hashCode();
        }
        return getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        T otherType = (T) other;

        if (getId() == null || otherType.getId() == null) {
            return false;
        }

        return new EqualsBuilder().append(otherType.getId(), getId()).isEquals();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
