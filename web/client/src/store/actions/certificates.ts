import {ICertificate} from "../certificate/certificateSlice";

export const GET_CERTIFICATE           = '[certificate] GET';
export const FETCH_CERTIFICATE_SUCCESS = '[certificate] Fetch success';
export const FETCH_CERTIFICATE_ERROR   = '[certificate] Fetch Error';
export const UPDATE_CERTIFICATE        = '[certificate] UPDATE';

export const getCertificate = (id: string) => ({
  type: GET_CERTIFICATE,
  payload: { id }
});

export const updateCertificate = (certificate: ICertificate) => ({
  type: "certificate/certificateLoaded",
  payload: certificate
});
