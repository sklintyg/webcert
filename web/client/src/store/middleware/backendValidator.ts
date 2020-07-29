import {Dispatch, Middleware, MiddlewareAPI} from "redux";
import {
  ICertificate,
  ICertificateState,
  validateCertificate
} from "../certificate/certificateSlice";
import { AnyAction } from "@reduxjs/toolkit";

const backendValidator: Middleware = ({getState, dispatch}: MiddlewareAPI) => (next: Dispatch) => action => {
  next(action);

  if (action.type === "certificate/certificateEditedNew") {
    const state: ICertificateState = getState().ui.uiCertificate;
    validate(state.certificate!, dispatch);
  }

};

function validate(certificate: ICertificate, dispatch: Dispatch<AnyAction>) {
  if (!certificate) return;

  dispatch(validateCertificate(certificate));
}

export default backendValidator;
