package se.inera.intyg.webcert.persistence.arende.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public interface ArendeDraftRepository extends CrudRepository<ArendeDraft, Long> {

    /**
     * Finds all the {@linkplain ArendeDraft} related to a certificate with id intygId.
     *
     * @param intygId
     *            The id of the certificate we are interested in.
     * @return {@linkplain List} of the drafts related to certificate
     */
    List<ArendeDraft> findByIntygId(String intygId);

    /**
     * Finds single {@linkplain ArendeDraft} with intygdId and questionId.
     *
     * @param intygId
     * @param questionId
     * @return
     */
    ArendeDraft findByIntygIdAndQuestionId(String intygId, String questionId);
}
