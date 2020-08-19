import {createAction} from "@reduxjs/toolkit";

export interface IApiCall {
  url: string;
  method: string;
  data: any;
  onStart?: string;
  onSuccess?: string;
  onError?: string;
}

export const apiCallBegan = createAction<IApiCall>("[API] Call began");
export const apiCallSuccess = createAction<any>("[API] Call success");
export const apiCallFailed = createAction<string>("[API] Call failed");
