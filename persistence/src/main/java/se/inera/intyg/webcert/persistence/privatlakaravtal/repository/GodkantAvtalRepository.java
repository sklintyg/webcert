package se.inera.webcert.persistence.privatlakaravtal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.webcert.persistence.privatlakaravtal.model.GodkantAvtal;

/**
 * Created by eriklupander on 2015-08-05.
 */
public interface GodkantAvtalRepository extends JpaRepository<GodkantAvtal, Long>, GodkantAvtalRepositoryCustom {
}
