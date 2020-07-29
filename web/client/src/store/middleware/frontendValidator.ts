import {Dispatch, Middleware, MiddlewareAPI} from "redux";
import {
  ICertificate,
  ICertificateState,
  visibleCertificate,
  ICertificateEditedNew, mandatoryCertificate
} from "../certificate/certificateSlice";
import { AnyAction } from "@reduxjs/toolkit";

const frontendValidator: Middleware = ({getState, dispatch}: MiddlewareAPI) => (next: Dispatch) => action => {
  next(action);

  if (action.type === "certificate/certificateEditedNew") {
    const state: ICertificateState = getState().ui.uiCertificate;
    validate(state.certificate!, dispatch, action.payload);
  }

};

function validate(certificate: ICertificate, dispatch: Dispatch<AnyAction>, update: ICertificateEditedNew) {
  if (!certificate) return;

  validateHideExpressions(certificate, dispatch, update);

  validateMandatory(certificate, dispatch, update);

}

function validateHideExpressions(certificate: ICertificate, dispatch: Dispatch<AnyAction>, update: ICertificateEditedNew) {
  const dataProp = certificate.data[update.id].config.prop;
  for (let questionId in certificate.data) {
    const question = certificate.data[questionId];
    if (question.validation && question.validation.hideExpression && question.validation.hideExpression.includes(dataProp)) {
      switch (update.data.type) {
        case "BOOLEAN":
          dispatch(visibleCertificate(question.id, update.data[dataProp] === 'true'));
          break;
        case "TEXT":
          dispatch(visibleCertificate(question.id, update.data[dataProp]));
          break;
        default:
          dispatch(visibleCertificate(question.id, update.data[dataProp]));
          break;
      }
    }
  }
}

function validateMandatory(certificate: ICertificate, dispatch: Dispatch<AnyAction>, update: ICertificateEditedNew) {
  const dataProp = certificate.data[update.id].config.prop;
  const question = certificate.data[update.id];
  if (question.validation && question.validation.required) {
    switch (update.data.type) {
      case "BOOLEAN":
        dispatch(mandatoryCertificate(question.id, !update.data[dataProp] && update.data[dataProp] === 'EMPTY'));
        break;
      case "TEXT":
        dispatch(mandatoryCertificate(question.id, !update.data[dataProp]));
        break;
      default:
        dispatch(mandatoryCertificate(question.id, !update.data[dataProp]));
        break;
    }
  }
}

export default frontendValidator;
