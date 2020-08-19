import {Certificate, CertificateDataElement, CertificateStatus, ValidationError} from "../domain/certificate";
import {createAction} from "@reduxjs/toolkit";

export const CERTIFICATE = '[CERTIFICATE]';
export const GET_CERTIFICATE = `${CERTIFICATE} Get`;
export const FETCH_CERTIFICATE_SUCCESS = `${CERTIFICATE} Fetch success`;
export const FETCH_CERTIFICATE_ERROR = `${CERTIFICATE} Fetch error`;

export const SIGN_CERTIFICATE = `${CERTIFICATE} Sign`;
export const SIGN_CERTIFICATE_SUCCESS = `${CERTIFICATE} Sign success`;
export const SIGN_CERTIFICATE_ERROR = `${CERTIFICATE} Sign error`;

export const UPDATE_CERTIFICATE_STATUS = `${CERTIFICATE} Update certificate status`;
export const UPDATE_CERTIFICATE_AS_READONLY = `${CERTIFICATE} Update certificate as readonly`;

export const UPDATE_CERTIFICATE = `${CERTIFICATE} Update`;
export const UPDATE_CERTIFICATE_DATA_ELEMENT = `${CERTIFICATE} Update data element`;
export const SET_CERTIFICATE_DATA_ELEMENT = `${CERTIFICATE} Set data element`;

export const VALIDATE_CERTIFICATE = `${CERTIFICATE} Validate`;
export const VALIDATE_CERTIFICATE_IN_FRONTEND = `${CERTIFICATE} Validate in frontend`;
export const VALIDATE_CERTIFICATE_STARTED = `${CERTIFICATE} Validation started`;
export const VALIDATE_CERTIFICATE_COMPLETED = `${CERTIFICATE} Validation completed`;
export const VALIDATE_CERTIFICATE_SUCCESS = `${CERTIFICATE} Validation success`;
export const VALIDATE_CERTIFICATE_ERROR = `${CERTIFICATE} Validation error`;
export const UPDATE_VALIDATION_ERRORS = `${CERTIFICATE} Update validation errors`;

export const SHOW_CERTIFICATE_DATA_ELEMENT = `${CERTIFICATE} Show data element`;
export const HIDE_CERTIFICATE_DATA_ELEMENT = `${CERTIFICATE} Hide data element`;

export const SHOW_CERTIFICATE_DATA_ELEMENT_MANDATORY = `${CERTIFICATE} Show mandatory on data element`;
export const HIDE_CERTIFICATE_DATA_ELEMENT_MANDATORY = `${CERTIFICATE} Hide mandatory on data element`;

export const SHOW_CERTIFICATE_LOADING_SPINNER = `${CERTIFICATE} Show spinner`;
export const HIDE_CERTIFICATE_LOADING_SPINNER = `${CERTIFICATE} Hide spinner`;

export const SHOW_CERTIFICATE_VALIDATION_ERRORS = `${CERTIFICATE} Show validation errors`;
export const HIDE_CERTIFICATE_VALIDATION_ERRORS = `${CERTIFICATE} Hide validation errors`;

export const getCertificate = createAction<string>(GET_CERTIFICATE);

export const signCertificate = createAction(SIGN_CERTIFICATE);

export const updateCertificateOld = createAction<Certificate>(UPDATE_CERTIFICATE);

export const updateCertificate = createAction<Certificate>(UPDATE_CERTIFICATE);

export const updateCertificateAsReadOnly = createAction(UPDATE_CERTIFICATE_AS_READONLY);

export const updateCertificateStatus = createAction<CertificateStatus>(UPDATE_CERTIFICATE_STATUS);

export const updateCertificateDataElement = createAction<CertificateDataElement>(UPDATE_CERTIFICATE_DATA_ELEMENT);

export const showCertificateDataElement = createAction<string>(SHOW_CERTIFICATE_DATA_ELEMENT);

export const hideCertificateDataElement = createAction<string>(HIDE_CERTIFICATE_DATA_ELEMENT);

export const showCertificateDataElementMandatory = createAction<string>(SHOW_CERTIFICATE_DATA_ELEMENT_MANDATORY);

export const hideCertificateDataElementMandatory = createAction<string>(HIDE_CERTIFICATE_DATA_ELEMENT_MANDATORY);

export const setCertificateDataElement = createAction<CertificateDataElement>(SET_CERTIFICATE_DATA_ELEMENT);

export const validateCertificate = createAction<Certificate>(VALIDATE_CERTIFICATE);

export const validateCertificateInFrontEnd = createAction<CertificateDataElement>(VALIDATE_CERTIFICATE_IN_FRONTEND);

export const showSpinner = createAction(SHOW_CERTIFICATE_LOADING_SPINNER);

export const hideSpinner = createAction(HIDE_CERTIFICATE_LOADING_SPINNER);

export const validateCertificateStarted = createAction(VALIDATE_CERTIFICATE_STARTED);

export const validateCertificateCompleted = createAction(VALIDATE_CERTIFICATE_COMPLETED);

export const updateValidationErrors = createAction<ValidationError[]>(UPDATE_VALIDATION_ERRORS);

export const showValidationErrors = createAction(SHOW_CERTIFICATE_VALIDATION_ERRORS);

export const hideValidationErrors = createAction(HIDE_CERTIFICATE_VALIDATION_ERRORS);
