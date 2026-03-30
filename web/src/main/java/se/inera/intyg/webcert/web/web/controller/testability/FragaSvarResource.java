/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.webcert.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.infra.security.common.model.Role;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;

/**
 * Bean for inserting questions directly into the database.
 *
 * <p>Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
@Api(value = "services fragasvar", description = "REST API för testbarhet - Fråga/Svar")
@RestController
@RequestMapping("/testability/fragasvartest")
@Profile({"dev", "testability-api"})
public class FragaSvarResource {

  private static final Logger LOG = LoggerFactory.getLogger(FragaSvarResource.class);

  @PersistenceContext private EntityManager entityManager;

  private TransactionTemplate transactionTemplate;

  @Autowired
  public void setTxManager(PlatformTransactionManager transactionManager) {
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @Autowired private FragaSvarService fragaSvarService;

  @Autowired private FragaSvarRepository fragasvarRepository;

  @Autowired private CommonAuthoritiesResolver authoritiesResolver;

  @PostMapping
  public ResponseEntity<FragaSvar> insertFragaSvar(@RequestBody FragaSvar fragaSvar) {
    FragaSvar saved = fragasvarRepository.save(fragaSvar);
    LOG.info("Created FragaSvar with id {} using testability API", saved.getInternReferens());
    return ResponseEntity.ok(saved);
  }

  @PutMapping("/svara/{vardgivare}/{enhet}/{id}")
  public ResponseEntity<FragaSvar> answer(
      @PathVariable("vardgivare") final String vardgivarId,
      @PathVariable("enhet") final String enhetsId,
      @PathVariable("id") final Long frageSvarId,
      @RequestBody String svarsText) {
    SecurityContext originalContext = SecurityContextHolder.getContext();
    SecurityContextHolder.setContext(getSecurityContext(vardgivarId, enhetsId));
    try {
      FragaSvar fragaSvarResponse = fragaSvarService.saveSvar(frageSvarId, svarsText);
      return ResponseEntity.ok(fragaSvarResponse);
    } finally {
      SecurityContextHolder.setContext(originalContext);
    }
  }

  @PostMapping("/skickafraga/{vardgivare}/{enhet}/{intygId}/{typ}")
  public ResponseEntity<FragaSvar> askQuestion(
      @PathVariable("vardgivare") final String vardgivarId,
      @PathVariable("enhet") final String enhetsId,
      @PathVariable("intygId") final String intygId,
      @PathVariable("typ") final String typ,
      @RequestBody CreateQuestionParameter parameter) {
    SecurityContext originalContext = SecurityContextHolder.getContext();
    SecurityContextHolder.setContext(getSecurityContext(vardgivarId, enhetsId));
    try {
      FragaSvar fragaSvarResponse =
          fragaSvarService.saveNewQuestion(
              intygId, typ, parameter.getAmne(), parameter.getFrageText());
      return ResponseEntity.ok(fragaSvarResponse);
    } finally {
      SecurityContextHolder.setContext(originalContext);
    }
  }

  @GetMapping("/extern/{externReferens}/translate")
  public ResponseEntity<FragaSvar> getFragaSvarByExternReferens(
      @PathVariable("externReferens") String externReferens) {
    return ResponseEntity.ok(fragasvarRepository.findByExternReferens(externReferens));
  }

  @DeleteMapping("/extern/{externReferens}")
  public ResponseEntity<Void> deleteFragaSvarByExternReferens(
      @PathVariable("externReferens") String externReferens) {
    List<FragaSvar> fragor = fragasvarRepository.findByExternReferensLike(externReferens);
    for (FragaSvar fraga : fragor) {
      fragasvarRepository.delete(fraga);
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/frageText/{frageText}")
  public ResponseEntity<Void> deleteFragaSvarByFrageText(
      @PathVariable("frageText") String frageText) {
    List<FragaSvar> fragorOchSvar = fragasvarRepository.findByFrageTextLike(frageText);
    if (fragorOchSvar != null) {
      for (FragaSvar fragaSvar : fragorOchSvar) {
        fragasvarRepository.delete(fragaSvar);
      }
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/svarsText/{svarsText}")
  public ResponseEntity<Void> deleteFragaSvarBySvarsText(
      @PathVariable("svarsText") String svarsText) {
    List<FragaSvar> fragorOchSvar = fragasvarRepository.findBySvarsTextLike(svarsText);
    if (fragorOchSvar != null) {
      for (FragaSvar fragaSvar : fragorOchSvar) {
        fragasvarRepository.delete(fragaSvar);
      }
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/enhet/{enhetsId}")
  public ResponseEntity<Void> deleteFragaSvarByEnhet(@PathVariable("enhetsId") String enhetsId) {
    List<String> enhetsIds = new ArrayList<>();
    enhetsIds.add(enhetsId);
    List<FragaSvar> fragorOchSvar = fragasvarRepository.findByEnhetsId(enhetsIds);
    if (fragorOchSvar != null) {
      for (FragaSvar fragaSvar : fragorOchSvar) {
        fragasvarRepository.delete(fragaSvar);
      }
    }
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFragaSvarById(@PathVariable("id") Long id) {
    fragasvarRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteAllFragaSvar() {
    return transactionTemplate.execute(
        new TransactionCallback<ResponseEntity<Void>>() {
          @Override
          public ResponseEntity<Void> doInTransaction(TransactionStatus status) {
            @SuppressWarnings("unchecked")
            List<FragaSvar> fragorOchSvar =
                entityManager.createQuery("SELECT f FROM FragaSvar f").getResultList();
            for (FragaSvar fragaSvar : fragorOchSvar) {
              entityManager.remove(fragaSvar);
            }
            return ResponseEntity.ok().build();
          }
        });
  }

  @GetMapping("/fragaSvarCount")
  public Long getFragaSvarCountCertificateIds(@RequestBody List<String> certificateIds) {
    final var fragaSvarList = fragasvarRepository.findAll();
    return fragaSvarList.stream()
        .filter(fragaSvar -> certificateIds.contains(fragaSvar.getIntygsReferens().getIntygsId()))
        .count();
  }

  @DeleteMapping("/byCertificateIds")
  public ResponseEntity<Void> deleteFragaSvarByCertificateIds(
      @RequestBody List<String> certificateIds) {
    final var fragaSvarList = fragasvarRepository.findAll();
    final var fragaSvarForDeletion =
        fragaSvarList.stream()
            .filter(
                fragaSvar -> certificateIds.contains(fragaSvar.getIntygsReferens().getIntygsId()))
            .collect(Collectors.toList());
    fragasvarRepository.deleteAll(fragaSvarForDeletion);

    return ResponseEntity.ok().build();
  }

  // Create a fake SecurityContext for a user which is authorized for the given care giver and unit
  @SuppressWarnings("serial")
  private SecurityContext getSecurityContext(final String vardgivarId, final String enhetsId) {
    final WebCertUser user = getWebCertUser(vardgivarId, enhetsId);

    return new SecurityContext() {

      @Override
      public void setAuthentication(Authentication authentication) {
        // Do nothing
      }

      @Override
      public Authentication getAuthentication() {
        return new Authentication() {
          @Override
          public Object getPrincipal() {
            return user;
          }

          @Override
          public boolean isAuthenticated() {
            return true;
          }

          @Override
          public String getName() {
            return "questionResource";
          }

          @Override
          public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
          }

          @Override
          public Object getCredentials() {
            return null;
          }

          @Override
          public Object getDetails() {
            return null;
          }

          @Override
          public void setAuthenticated(boolean isAuthenticated) {
            // Do nothing
          }
        };
      }
    };
  }

  // Create a fake WebCertUser which is authorized for the given care giver and unit
  private WebCertUser getWebCertUser(String vardgivarId, String enhetsId) {
    WebCertUser user = new WebCertUser();

    Role role = authoritiesResolver.getRole(AuthoritiesConstants.ROLE_LAKARE);
    user.setRoles(AuthoritiesResolverUtil.toMap(role));
    user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

    user.setHsaId("questionResource");
    user.setNamn("questionResource");
    user.setForskrivarkod("questionResource");

    List<Vardgivare> vardgivarList = new ArrayList<>();
    Vardgivare vardgivare = new Vardgivare(vardgivarId, "questionResource");
    List<Vardenhet> vardenheter = new ArrayList<>();
    Vardenhet enhet = new Vardenhet(enhetsId, "questionResource");
    vardenheter.add(enhet);
    vardgivare.setVardenheter(vardenheter);
    vardgivarList.add(vardgivare);

    user.setVardgivare(vardgivarList);
    user.setValdVardgivare(vardgivare);
    user.setValdVardenhet(enhet);

    return user;
  }
}
