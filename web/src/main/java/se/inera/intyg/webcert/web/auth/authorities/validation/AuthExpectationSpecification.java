/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.auth.authorities.validation;

import se.inera.intyg.webcert.web.security.WebCertUserOriginType;

/**
 * Created by marced on 18/12/15.
 */
public interface AuthExpectationSpecification {
    /**
     * To pass, the user must have at least one of the given features active.
     * Also, if intygstyp context is given, that intygsmodule must also have given feature active.
     * <p/>
     * If multiple featureConstraints are given, this is effectively an OR condition. To express an AND condition you
     * can simply chain multiple features("x").features("y")
     *
     * @param featureConstraints
     * @return
     */
    AuthExpectationSpecification features(String... featureConstraints);

    /**
     * To pass, the user must NOT have ANY of the given features active.
     * Also, if intygstyp context is given that intygsmodule must also have given feature active to be considered
     * active.
     * <p/>
     *
     * @param featureConstraints
     * @return
     */
    AuthExpectationSpecification notFeatures(String... featureConstraints);

    /**
     * To pass, the user's request origin must match one of the given validOriginTypes.
     * <p/>
     * If multiple validOriginTypes are given, this is effectively an OR condition. To express an AND condition you can
     * simply chain multiple origins(type1).origins(type2)
     *
     * @param validOriginTypes
     * @return
     */
    AuthExpectationSpecification origins(WebCertUserOriginType... validOriginTypes);

    /**
     * To pass, the user's request origin must NOT match any of the given invalidOriginTypes.
     *
     * @param invalidOriginTypes
     * @return
     */
    AuthExpectationSpecification notOrigins(WebCertUserOriginType... invalidOriginTypes);

    /**
     * To pass, the user's must have a role matching one of the given validRoles.
     * <p/>
     * If multiple validRoles are given, this is effectively an OR condition. To express an AND condition you can simply
     * chain multiple roles(type1).roles(type2)
     *
     * @param validRoles
     * @return
     */
    AuthExpectationSpecification roles(String... validRoles);

    /**
     * To pass, the user's role must NOT match any of the given invalidRoles.
     *
     * @param invalidRoles
     * @return
     */
    AuthExpectationSpecification notRoles(String... invalidRoles);

    /**
     * To pass, the following must be fulfilled.
     * <ul>
     * <li>The user must have the basic privilegeConstraint.</li>
     * <li>If the privilegeConstraint's has a intygstyp constraint - that must match a given intygstype context.</li>
     * <li>If the privilegeConstraint's has a requestOrigin constraint - that must match the users origin.</li>
     * <li>If the privilegeConstraint's requestOrigin constraint itself has a intygstype constraint - that must also
     * match given intygstype context.</li>
     * </ul>
     *
     * @param privilegeConstraint
     * @return
     */
    AuthExpectationSpecification privilege(String privilegeConstraint);

    /**
     * A negation of privilege method to be able to express privilege a user must NOT.
     *
     * @param privilegeConstraint
     * @return
     */
    AuthExpectationSpecification notPrivilege(String privilegeConstraint);

    /**
     * Returns true if all added checks passes, otherwise false.
     * @return
     */
    boolean isVerified();

    /**
     * Throws AuthoritiesException if any of the added check fails.
     * @return
     */
    void orThrow();
}
