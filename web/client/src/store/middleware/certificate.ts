import {Dispatch, Middleware, MiddlewareAPI} from "redux";
import { AnyAction } from "@reduxjs/toolkit";
import {FETCH_CERTIFICATE_ERROR, FETCH_CERTIFICATE_SUCCESS, GET_CERTIFICATE, updateCertificate} from "../actions/certificates";
import {apiCallBegan} from "../api";

/**
 * Load a certificate
 */
const getCertificate : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type === GET_CERTIFICATE) {
    apiCallBegan({
      url: "/api/certificate",
      method: "get",
      data: {
        id: action.payload.id
      },
      onSuccess: FETCH_CERTIFICATE_SUCCESS,
      onError: FETCH_CERTIFICATE_ERROR,
    });
  }
}

const processCertificate : Middleware<Dispatch> = ({ dispatch }: MiddlewareAPI) => next => (action: AnyAction) => {
  next(action);

  if (action.type === FETCH_CERTIFICATE_SUCCESS) {
    dispatch(updateCertificate(action.payload));
  }
}
