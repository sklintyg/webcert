package se.inera.certificate.mc2wc.medcert.jpa.model;

public interface Entity<T, ID> {
    /**
     * Entities have an identity.
     * 
     * @return The identity of this entity.
     */
    ID getId();

    /**
     * Entities compare by identity, not by attributes.
     * 
     * @param other
     *            The other entity.
     * @return true if the identities are the same, regardless of other attributes.
     */
    boolean sameAs(T other);
    
}
