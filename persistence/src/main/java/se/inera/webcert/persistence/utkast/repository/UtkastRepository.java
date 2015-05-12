package se.inera.webcert.persistence.utkast.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.inera.webcert.persistence.utkast.model.Utkast;

public interface UtkastRepository extends JpaRepository<Utkast, String>, UtkastRepositoryCustom {

}
