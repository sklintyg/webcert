import {CaseReducer, createSlice, PayloadAction} from "@reduxjs/toolkit";
import {apiCallBegan} from "../api";
import {RootState} from "../store";

export interface ICertificateState {
  readonly loading: boolean;
  readonly signing: boolean;
  readonly validationNeeded: boolean;
  readonly validating: boolean;
  readonly showValidationError: boolean;
  readonly saved: boolean;
  readonly error?: string;
  readonly certificate?: ICertificate;
  readonly certificateStructure?: ICertificateStructure[];
}

export interface ICertificateStructure {
  readonly id: string;
  readonly component: string;
}

export interface ICertificate {
  readonly metadata: ICertificateMetadata;
  readonly data: ICertificateData;
}

export interface ICertificateMetadata {
  readonly certificateCode: string;
  readonly certificateId: string;
  readonly certificateName: string;
  readonly signed: boolean;
}

export interface ICertificateData {
  [propName: string]: ICertificateContent;
}

export interface ICertificateContent {
  readonly id: string;
  readonly parent: string;
  readonly index: number;
  readonly visible: boolean;
  readonly readOnly: boolean;
  readonly mandatory: boolean;
  readonly config: ICertificateContentConfig;
  readonly data: ICertificateContentData;
  readonly validation: ICertificateContentValidation;
  readonly validationError: IValidationError[];
}

export interface IValidationError {
  readonly id: string;
  readonly category: string;
  readonly field: string;
  readonly type: string;
  readonly text: string;
}

export interface ICertificateContentConfig {
  readonly text: string;
  readonly description: string;
  readonly component: string;
  readonly prop: string;
}

export interface ICertificateContentData {
  readonly type: "BOOLEAN" | "TEXT";
  readonly [propName: string]: any;
}

export interface ICertificateContentValidation {
  readonly required: boolean;
  readonly requiredProp: string;
  readonly hideExpression: string;
}

const initialCertificateState: ICertificateState = {
  loading: false,
  signing: false,
  saved: true,
  showValidationError: false,
  validating: false,
  validationNeeded: false,
};

// REDUCERS
const certificateRequested: CaseReducer<ICertificateState, PayloadAction> = (state) => {
  state.loading = true;
}

const certificateBeingValidated: CaseReducer<ICertificateState, PayloadAction> = (state) => {
  state.validating = true;
  state.validationNeeded = false;
}

const certificateBeingSigned: CaseReducer<ICertificateState, PayloadAction> = (state) => {
  state.signing = true;
}

const certificateSigned: CaseReducer<ICertificateState, PayloadAction> = (state) => {
  state.signing = false;
  state.certificate!.metadata.signed = true;
  for (let questionId in state.certificate!.data)
    state.certificate!.data[questionId].readOnly = true;
}

const certificateVisible: CaseReducer<ICertificateState, PayloadAction<ICertificateVisible>> = (state, action) => {
  state.certificate!.data[action.payload.id].visible = action.payload.visible;
}

const certificateMandatory: CaseReducer<ICertificateState, PayloadAction<ICertificateMandatory>> = (state, action) => {
  state.certificate!.data[action.payload.id].mandatory = action.payload.mandatory;
}

export const certificateEdited: CaseReducer<ICertificateState, PayloadAction<ICertificateEdited>> = (state, action) => {
  state.certificate!.data[action.payload.id].data[action.payload.prop] = action.payload.value;
  state.saved = false;
}

export const certificateEditedNew: CaseReducer<ICertificateState, PayloadAction<ICertificateEditedNew>> = (state, action) => {
  state.certificate!.data[action.payload.id].data = action.payload.data;
  state.saved = false;
  state.validationNeeded = true;
}

const certificateLoaded: CaseReducer<ICertificateState, PayloadAction<ICertificate>> = (state, action) => {
  state.certificate = action.payload;
  state.certificateStructure = new Array(Object.keys(state.certificate.data).length);
  for (let questionId in state.certificate.data) {
    state.certificateStructure[state.certificate.data[questionId].index] = {
      id: state.certificate.data[questionId].id,
      component: state.certificate.data[questionId].config.component
    };
  }
  state.loading = false;
  state.validationNeeded = !state.certificate.metadata.signed;
}

const certificateError: CaseReducer<ICertificateState, PayloadAction<string>> = (state, action) => {
  state.loading = false;
  state.error = action.payload;
}

const certificateValidationBeingShown: CaseReducer<ICertificateState, PayloadAction> = (state, action) => {
  state.showValidationError = true;
}

const certificateSaved: CaseReducer<ICertificateState, PayloadAction> = (state) => {
  state.saved = true;
}

const certificateValidated: CaseReducer<ICertificateState, PayloadAction<IValidationError[]>> = (state, action) => {
  state.validating = false;
  for (let questionId in state.certificate!.data) {
    const question = state.certificate!.data[questionId];
    if (question.config.component === 'category') {
      continue;
    }

    question.validationError = [];
    for (let validationError of action.payload) {
      if (validationError.id === questionId) {
        question.validationError.push(validationError);
      }
    }
  }
}

export const certificateSlice = createSlice({
  name: "certificate",
  initialState: initialCertificateState,
  reducers: {
    certificateRequested,
    certificateBeingSigned,
    certificateBeingValidated,
    certificateLoaded,
    certificateError,
    certificateSaved,
    certificateSigned,
    certificateEdited,
    certificateEditedNew,
    certificateVisible,
    certificateMandatory,
    certificateValidated,
    certificateValidationBeingShown,
  },
});

// ACTION CREATORS
export const loadCertificate = (id: string) =>
  apiCallBegan({
    url: "/api/certificate",
    method: "get",
    data: {
      id
    },
    onStart: "certificate/certificateRequested",
    onSuccess: "certificate/certificateLoaded",
    onError: "certificate/certificateError",
  });

export const signCertificate = (id: string) => (dispatch: any, getState: any) => {
  const certificate: ICertificate = getState().ui.uiCertificate.certificate;
  for (let questionId in certificate.data) {
    if (certificate.data[questionId].visible && certificate.data[questionId].validationError && certificate.data[questionId].validationError.length > 0) {
      dispatch(showValidationErrorForCertificate());
      return;
    }
  }

  dispatch(apiCallBegan({
    url: "/api/certificate",
    method: "post",
    data: {id},
    onStart: "certificate/certificateBeingSigned",
    onSuccess: "certificate/certificateSigned",
  }));
}

export const showValidationErrorForCertificate = () => certificateSlice.actions.certificateValidationBeingShown();

export const validateCertificateNew = () => (dispatch: any, getState: any) => {
  const certificate: ICertificate = getState().ui.uiCertificate.certificate;
  if (certificate == null) return;

  dispatch(apiCallBegan({
    url: "/api/certificate/validate",
    method: "post",
    data: certificate,
    onStart: "certificate/certificateBeingValidated",
    onSuccess: "certificate/certificateValidated",
  }));
}

export const validateCertificate = (certificate: ICertificate) =>
  apiCallBegan({
    url: "/api/certificate/validate",
    method: "post",
    data: certificate,
    onStart: "certificate/certificateBeingValidated",
    onSuccess: "certificate/certificateValidated",
  });

interface ICertificateEdited {
  id: string;
  prop: string;
  value: any;
}

export const editCertificate = (id: string, prop: string, value: any) =>
  certificateSlice.actions.certificateEdited({id, prop, value});

export interface ICertificateEditedNew {
  id: string;
  data: ICertificateContentData
}

export const editCertificateNew = (id: string, data: ICertificateContentData) =>
  certificateSlice.actions.certificateEditedNew({id, data});

interface ICertificateVisible {
  id: string;
  visible: boolean;
}

export const visibleCertificate = (id: string, visible: boolean) =>
  certificateSlice.actions.certificateVisible({id, visible});

interface ICertificateMandatory {
  id: string;
  mandatory: boolean;
}

export const mandatoryCertificate = (id: string, mandatory: boolean) =>
  certificateSlice.actions.certificateMandatory({id, mandatory});

export const saveCertificate = (certificate: ICertificate) =>
  certificateSlice.actions.certificateSaved();

// SELECTORS
export const isLoading = (state: RootState) => false;

export const isSigning = (state: RootState) => false;

export const isValidationNeeded = (state: RootState) => false;

export const showValidationError = (state: RootState) => false;

export const getError = (state: RootState) => "";

export const getCertificateStructure = (state: RootState) => null;

export const getQuestion = (id: string) => (state: RootState) => state.ui.uiCertificate.certificate!.data[id];

export const getCertificate = (state: RootState) => state.ui.uiCertificate.certificate;

export const getCertificateState = (state: RootState) => state.ui.uiCertificate;

export default certificateSlice.reducer;
