/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.utkast.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class UtkastRepositoryCustomTest {

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private UtkastRepositoryCustom utkastRepositoryCustom;

    /**
     * Testing several @Query methods in same test to save setup/teardown time.
     */
    @Test
    public void testFindRelations() {
        // Create two hierarchies
        String intygIdParent = "intyg-1";
        String intygIdChild1 = "intyg-1-1";
        String intygIdChild2 = "intyg-1-2";
        String intygIdChild3 = "intyg-1-3";

        String intygId2Parent = "intyg-2";
        String intygId2Child1 = "intyg-2-1";
        String intygId2Child2 = "intyg-2-2";
        String intygId2Child3 = "intyg-2-3";

        // Hierarchy 1, one parent, three children
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdParent, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, null, null,
                LocalDateTime.now().minusDays(10L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdChild1, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, intygIdParent,
                RelationKod.ERSATT, LocalDateTime.now().minusDays(5L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdChild2, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE,
                intygIdParent, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdChild3, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE,
                intygIdParent, RelationKod.FRLANG, LocalDateTime.now().minusDays(1L)));
        
        // Hierarchy 2, one parent, three children as well
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId2Parent, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, null, null,
                LocalDateTime.now().minusDays(10L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId2Child1, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, intygId2Parent,
                RelationKod.ERSATT, LocalDateTime.now().minusDays(5L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId2Child2, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE,
                intygId2Parent, RelationKod.KOMPLT, LocalDateTime.now().minusDays(2L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId2Child3, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE,
                intygId2Parent, RelationKod.FRLANG, LocalDateTime.now().minusDays(1L)));

        // Check the parent
        List<WebcertCertificateRelation> parentRelations = utkastRepositoryCustom.findParentRelation(intygIdChild1);

        assertNotNull(parentRelations);
        assertEquals(1, parentRelations.size());
        assertEquals(intygIdParent, parentRelations.get(0).getIntygsId());
        assertEquals(RelationKod.ERSATT, parentRelations.get(0).getRelationKod());

        // Check children
        List<WebcertCertificateRelation> childRelations = utkastRepositoryCustom.findChildRelations(intygIdParent);
        childRelations.sort((cr1, cr2) -> cr2.getSkapad().compareTo(cr1.getSkapad()));

        assertEquals(3, childRelations.size());
        assertEquals(intygIdChild3, childRelations.get(0).getIntygsId());
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE, childRelations.get(0).getStatus());

        assertEquals(intygIdChild2, childRelations.get(1).getIntygsId());
        assertEquals(UtkastStatus.DRAFT_COMPLETE, childRelations.get(1).getStatus());

        assertEquals(intygIdChild1, childRelations.get(2).getIntygsId());
        assertEquals(UtkastStatus.SIGNED, childRelations.get(2).getStatus());
    }

    @Test
    public void testRemoveRelationsToDraft() {
        // Create two hierarchies
        String intygIdParent = "intyg-1";
        String intygIdChild1 = "intyg-1-1";

        // Hierarchy 1, one parent, three children
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdParent, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, null, null,
                LocalDateTime.now().minusDays(10L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygIdChild1, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, intygIdParent,
                RelationKod.ERSATT, LocalDateTime.now().minusDays(5L)));


        // Verify relation
        Utkast intygChild1 = utkastRepository.findOne(intygIdChild1);

        assertEquals(RelationKod.ERSATT, intygChild1.getRelationKod());
        assertEquals(intygIdParent, intygChild1.getRelationIntygsId());

        // Remove relations
        utkastRepositoryCustom.removeRelationsToDraft(intygIdParent);

        intygChild1 = utkastRepository.findOne(intygIdChild1);

        assertNull(intygChild1.getRelationKod());
        assertNull(intygChild1.getRelationIntygsId());
    }

    @Test
    public void testFindDraftsByNotLockedOrSignedAndSkapadBefore() {
        // Create two hierarchies
        String intygId_signed = "intyg-1";
        String intygId_locked = "intyg-2";
        String intygId_draft_complete = "intyg-3";
        String intygId_draft_incomplete = "intyg-4";

        String intygId_draft_10_days = "intyg-10";
        String intygId_draft_14_days = "intyg-14";
        String intygId_draft_15_days = "intyg-15";
        String intygId_draft_20_days = "intyg-20";


        // Hierarchy 1, one parent, three children
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_signed, UtkastTestUtil.ENHET_1_ID, UtkastStatus.SIGNED, null, null,
                LocalDateTime.now().minusDays(20L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_locked, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_LOCKED, null,
                null, LocalDateTime.now().minusDays(25L)));


        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_complete, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_COMPLETE, null,
                null, LocalDateTime.now().minusDays(25L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_incomplete, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE, null,
                null, LocalDateTime.now().minusDays(25L)));

        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_10_days, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE, null,
                null, LocalDateTime.now().minusDays(10L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_14_days, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE, null,
                null, LocalDateTime.now().minusDays(14L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_15_days, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE, null,
                null, LocalDateTime.now().minusDays(15L)));
        utkastRepository.save(UtkastTestUtil.buildUtkast(intygId_draft_20_days, UtkastTestUtil.ENHET_1_ID, UtkastStatus.DRAFT_INCOMPLETE, null,
                null, LocalDateTime.now().minusDays(20L)));


        LocalDateTime skapad = LocalDate.now().minusDays(14L).atStartOfDay();
        List<Utkast> utkasts = utkastRepositoryCustom.findDraftsByNotLockedOrSignedAndSkapadBefore(skapad);

        List<String> ids = utkasts.stream().map(Utkast::getIntygsId).collect(Collectors.toList());
        List<String> intygsIdn = Arrays.asList(intygId_draft_complete, intygId_draft_incomplete, intygId_draft_15_days, intygId_draft_20_days);

        assertEquals(4, utkasts.size());
        assertTrue(ids.containsAll(intygsIdn));
    }
}
