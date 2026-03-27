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
package se.inera.intyg.webcert.infra.srs.model;

public class SrsRecommendation {

  private String recommendationTitle;
  private String recommendationText;

  public static SrsRecommendation create(String recommendationTitle, String recommendationText) {
    SrsRecommendation recommendation = new SrsRecommendation();
    recommendation.setRecommendationTitle(recommendationTitle);
    recommendation.setRecommendationText(recommendationText);
    return recommendation;
  }

  public String getRecommendationText() {
    return this.recommendationText;
  }

  public void setRecommendationText(String recommendationText) {
    this.recommendationText = recommendationText;
  }

  public String getRecommendationTitle() {
    return recommendationTitle;
  }

  public void setRecommendationTitle(String recommendationTitle) {
    this.recommendationTitle = recommendationTitle;
  }
}
