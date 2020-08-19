import {Dispatch, Middleware, MiddlewareAPI} from "redux";
import {AnyAction} from "@reduxjs/toolkit";
import {
  FETCH_CERTIFICATE_ERROR,
  FETCH_CERTIFICATE_SUCCESS,
  GET_CERTIFICATE,
  hideCertificateDataElement,
  hideCertificateDataElementMandatory,
  hideSpinner,
  hideValidationErrors,
  setCertificateDataElement,
  showCertificateDataElement,
  showCertificateDataElementMandatory,
  showSpinner,
  showValidationErrors,
  SIGN_CERTIFICATE,
  SIGN_CERTIFICATE_ERROR,
  SIGN_CERTIFICATE_SUCCESS,
  UPDATE_CERTIFICATE_DATA_ELEMENT,
  updateCertificate,
  updateCertificateAsReadOnly,
  updateCertificateStatus,
  updateValidationErrors,
  VALIDATE_CERTIFICATE,
  VALIDATE_CERTIFICATE_ERROR,
  VALIDATE_CERTIFICATE_IN_FRONTEND,
  VALIDATE_CERTIFICATE_SUCCESS,
  validateCertificate,
  validateCertificateCompleted,
  validateCertificateInFrontEnd,
  validateCertificateStarted
} from "../actions/certificates";
import {apiCallBegan} from "../api";
import {
  Certificate,
  CertificateBooleanValue,
  CertificateDataElement,
  CertificateDataValueType,
  CertificateStatus,
  CertificateTextValue
} from "../domain/certificate";

/**
 * Load a certificate
 */
const handleGetCertificate : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type !== GET_CERTIFICATE) {
    return;
  }

  dispatch(showSpinner());

  dispatch(apiCallBegan({
      url: "/api/certificate",
      method: "get",
      data: {
        id: action.payload
      },
      onSuccess: FETCH_CERTIFICATE_SUCCESS,
      onError: FETCH_CERTIFICATE_ERROR,
    })
  );
}

/**
 * Process a loaded certificate
 */
const handleFetchCertificateSuccess : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type !== FETCH_CERTIFICATE_SUCCESS) {
    return;
  }

  dispatch(updateCertificate(action.payload));
  dispatch(hideSpinner());
  dispatch(validateCertificate(action.payload));
}

const handleSignCertificate : Middleware<Dispatch> = ({ dispatch, getState }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type !== SIGN_CERTIFICATE) {
    return;
  }

  const certificate: Certificate = getState().ui.uiCertificate.certificate;
  for (let questionId in certificate.data) {
    if (certificate.data[questionId].visible && certificate.data[questionId].validationErrors && certificate.data[questionId].validationErrors.length > 0) {
      dispatch(showValidationErrors());
      return;
    }
  }

  dispatch(showSpinner());

  dispatch(apiCallBegan({
    url: "/api/certificate",
    method: "post",
    data: { id: certificate.metadata.certificateId },
    onSuccess: SIGN_CERTIFICATE_SUCCESS,
    onError: SIGN_CERTIFICATE_ERROR,
  }));
}

const handleSignCertificateSuccess : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type !== SIGN_CERTIFICATE_SUCCESS) {
    return;
  }

  dispatch(hideValidationErrors());
  dispatch(updateCertificateStatus(CertificateStatus.SIGNED));
  dispatch(updateCertificateAsReadOnly());
  dispatch(hideSpinner());
}

const handleCertificateDataElementUpdate : Middleware<Dispatch> = ({ dispatch, getState }: MiddlewareAPI) => next => (action:AnyAction) => {
  next(action);

  if (action.type !== UPDATE_CERTIFICATE_DATA_ELEMENT) {
    return;
  }

  dispatch(setCertificateDataElement(action.payload));
  dispatch(validateCertificateInFrontEnd(action.payload));
  dispatch(validateCertificate(getState().ui.uiCertificate.certificate));
}

/**
 * Backend validation of a certificate
 */
const handleValidateCertificate : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action:AnyAction) => {
  next(action);

  if (action.type !== VALIDATE_CERTIFICATE) {
    return;
  }

  dispatch(validateCertificateStarted());

  dispatch(apiCallBegan({
    url: "/api/certificate/validate",
    method: "post",
    data: action.payload,
    onSuccess: VALIDATE_CERTIFICATE_SUCCESS,
    onError: VALIDATE_CERTIFICATE_ERROR,
  }));
}

const handleValidateCertificateInFrontEnd : Middleware<Dispatch> = ({ dispatch, getState }: MiddlewareAPI) => next => (action:AnyAction) => {
  next(action);

  if (action.type !== VALIDATE_CERTIFICATE_IN_FRONTEND) {
    return;
  }

  validate(getState().ui.uiCertificate.certificate, dispatch, action.payload);
}

const handleValidateCertificateSuccess : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type !== VALIDATE_CERTIFICATE_SUCCESS) {
    return;
  }

  dispatch(updateValidationErrors(action.payload));
  dispatch(validateCertificateCompleted());
}

function validate(certificate: Certificate, dispatch: Dispatch<AnyAction>, update: CertificateDataElement) {
  if (!certificate) return;

  validateHideExpressions(certificate, dispatch, update);

  validateMandatory(certificate, dispatch, update);

}

function validateHideExpressions(certificate: Certificate, dispatch: Dispatch<AnyAction>, update: CertificateDataElement) {
  const dataProp = certificate.data[update.id].config.prop;
  for (let questionId in certificate.data) {
    const question = certificate.data[questionId];
    if (question.validation && question.validation.hideExpression && question.validation.hideExpression.includes(dataProp)) {
      switch (update.value.type) {
        case CertificateDataValueType.BOOLEAN:
          const booleanValue = (update.value as CertificateBooleanValue).selected;
          if (booleanValue && !question.visible) {
            dispatch(showCertificateDataElement(questionId));
          } else if (!booleanValue && question.visible) {
            dispatch(hideCertificateDataElement(questionId));
          }
          break;
        case CertificateDataValueType.TEXT:
          const textValue = (update.value as CertificateTextValue).text;
          if (textValue != null && textValue.length > 0) {
            dispatch(showCertificateDataElement(questionId));
          } else if (question.visible) {
            dispatch(hideCertificateDataElement(questionId));
          }
          break;
        default:
          break;
      }
    }
  }
}

function validateMandatory(certificate: Certificate, dispatch: Dispatch<AnyAction>, update: CertificateDataElement) {
  const question = certificate.data[update.id];
  if (question.validation && question.validation.required) {
    switch (update.value.type) {
      case "BOOLEAN":
        const booleanValue = (update.value as CertificateBooleanValue).selected;
        if (booleanValue === null) {
          dispatch(showCertificateDataElementMandatory(question.id));
        } else if (question.mandatory) {
          dispatch(hideCertificateDataElementMandatory(question.id));
        }
        break;
      case "TEXT":
        const textValue = (update.value as CertificateTextValue).text;
        if (textValue === null || textValue.length === 0) {
          dispatch(hideCertificateDataElementMandatory(question.id));
        } else if (question.mandatory) {
          dispatch(hideCertificateDataElementMandatory(question.id));
        }
        break;
      default:
        break;
    }
  }
}

export const certificateMiddleware = [handleGetCertificate, handleFetchCertificateSuccess, handleSignCertificate, handleSignCertificateSuccess, handleCertificateDataElementUpdate, handleValidateCertificateInFrontEnd, handleValidateCertificate, handleValidateCertificateSuccess];
