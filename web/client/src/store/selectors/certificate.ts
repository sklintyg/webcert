import {RootState} from "../store";
import {createSelector} from "@reduxjs/toolkit";
import {Certificate, CertificateData} from "../domain/certificate";


export const getIsLoading = (state: RootState) => state.ui.uiCertificate.spinner;

export const getIsSigning = (state: RootState) => state.ui.uiCertificate.spinner;

export const getIsValidating = (state: RootState) => state.ui.uiCertificate.validationInProgress;

export const getShowValidationErrors = (state: RootState) => state.ui.uiCertificate.showValidationErrors;

export const getCertificate = (state: RootState): Certificate => state.ui.uiCertificate.certificate!;

export const getCertificateMetaData = (state: RootState) => {
  const { certificate } = state.ui.uiCertificate;
  if (!certificate) {
    return null;
  }

  return certificate.metadata;
}

export interface CertificateStructure {
  id: string;
  component: string;
  index: number;
}

const certificateStructure: CertificateStructure[] = [];
export const getCertificateDataElements = createSelector<RootState, Certificate, CertificateStructure[]>(
  getCertificate,
  (certificate) => {
    certificateStructure.length = 0;
    if (!certificate) {
      return [];
    }

    for (let questionId in certificate.data) {
      certificateStructure.push({
        id: certificate.data[questionId].id,
        component: certificate.data[questionId].config.component,
        index: certificate.data[questionId].index
      });
    }

    certificateStructure.sort((a, b) => a.index - b.index);
    return certificateStructure;
  }
)
