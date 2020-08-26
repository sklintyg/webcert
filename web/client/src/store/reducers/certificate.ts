import {createReducer} from "@reduxjs/toolkit";
import {Certificate} from "../domain/certificate";
import {
  hideCertificateDataElement, hideCertificateDataElementMandatory,
  hideSpinner, hideValidationErrors, setCertificateDataElement, showCertificateDataElement, showCertificateDataElementMandatory,
  showSpinner, showValidationErrors,
  updateCertificate, updateCertificateAsReadOnly, updateCertificateStatus, updateValidationErrors,
  validateCertificateCompleted,
  validateCertificateStarted
} from "../actions/certificates";

interface CertificateState {
  certificate?: Certificate;
  spinner: boolean;
  spinnerText: string;
  validationInProgress: boolean;
  showValidationErrors: boolean;
  isValidForSigning: boolean;
}

const initialState: CertificateState = {
  spinner: false,
  spinnerText: "",
  validationInProgress: false,
  showValidationErrors: false,
  isValidForSigning: false,
};

const certificateReducer = createReducer(initialState, builder =>
  builder
    .addCase(updateCertificate, (state, action) => {
      state.certificate = action.payload;
    })
    .addCase(updateCertificateStatus, ((state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.metadata.status  = action.payload;
    }))
    .addCase(updateCertificateAsReadOnly, (state => {
      if (!state.certificate) {
        return;
      }

      for (let questionId in state.certificate!.data) {
        state.certificate.data[questionId].readOnly = true;
      }
    }))
    .addCase(setCertificateDataElement, (state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.data[action.payload.id] = action.payload;
    })
    .addCase(showSpinner, (state, action) => {
      state.spinner = true;
      state.spinnerText = action.payload;
    })
    .addCase(hideSpinner, (state => {
      state.spinner = false;
      state.spinnerText = "";
    }))
    .addCase(validateCertificateStarted, (state => {
      state.validationInProgress = true;
    }))
    .addCase(validateCertificateCompleted, (state => {
      state.validationInProgress = false;
    }))
    .addCase(updateValidationErrors, ((state, action) => {
      for (let questionId in state.certificate!.data) {
        const question = state.certificate!.data[questionId];
        if (question.config.component === 'category') {
          continue;
        }

        question.validationErrors = [];
        for (let validationError of action.payload) {
          if (validationError.id === questionId) {
            question.validationErrors.push(validationError);
          }
        }
      }
      state.isValidForSigning = action.payload.length === 0;
    }))
    .addCase(showValidationErrors, (state => {
      state.showValidationErrors = true;
    }))
    .addCase(hideValidationErrors, (state => {
      state.showValidationErrors = false;
    }))
    .addCase(showCertificateDataElement, (state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.data[action.payload].visible = true;
    })
    .addCase(hideCertificateDataElement, (state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.data[action.payload].visible = false;
    })
    .addCase(showCertificateDataElementMandatory, (state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.data[action.payload].mandatory = true;
    })
    .addCase(hideCertificateDataElementMandatory, (state, action) => {
      if (!state.certificate) {
        return;
      }

      state.certificate.data[action.payload].mandatory = false;
    })
);

export default certificateReducer;
