import {createAction} from "@reduxjs/toolkit";

export interface IApiCall {
  url: string;
  method: string;
  data: any;
  onStart?: string;
  onSuccess?: string;
  onError?: string;
}

export const apiCallBegan = createAction<IApiCall>("api/callBegan");
export const apiCallSuccess = createAction<any>("api/callSuccess");
export const apiCallFailed = createAction<string>("api/callFailed");
