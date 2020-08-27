import * as actions from "../api";
import { Middleware, MiddlewareAPI, Dispatch } from "redux";
import demoDraft from "../certificate/bed26d3e-7112-4f08-98bf-01be40e26c80.json";
import demoDraft2 from "../certificate/bed26d3e-7112-4f08-98bf-01be40e26c90.json";
import {Certificate, CertificateBooleanValue, CertificateTextValue, ValidationError} from "../domain/certificate";

const api: Middleware = ({ getState, dispatch }: MiddlewareAPI) => (next: Dispatch) => async action => {
  if (action.type !== actions.apiCallBegan.type) return next(action);

  const { url, method, data, onStart, onSuccess, onError } = action.payload;

  if (onStart) dispatch({ type: onStart });

  next(action);

  try {
    // MWW: Here we would make the api call. For now just simulate a async call.
    const response = simulateApiCall(url, method, data);
    await stall(1000);

    // General
    dispatch(actions.apiCallSuccess(response.data));
    // Specific
    if (onSuccess) dispatch({ type: onSuccess, payload: response.data });
  } catch (error) {
    // General
    dispatch(actions.apiCallFailed(error.message));
    // Specific
    if (onError) dispatch({ type: onError, payload: error.message });
  }
};

async function stall(stallTime = 3000) {
  await new Promise((resolve) => setTimeout(resolve, stallTime));
}

// MWW: Just return the demodraft if get, otherwise just an empty object.
const simulateApiCall = (url: string, method: string, data: any) => {
  if (method === "get") {
    switch(data.id) {
      case "bed26d3e-7112-4f08-98bf-01be40e26c80":
        return { data: demoDraft };
      case "bed26d3e-7112-4f08-98bf-01be40e26c90":
        return { data: demoDraft2 };
      default:
        throw new Error("No certificate with this id: " + data.id);
    }
  } else if (method === "post") {
    const certificate: Certificate = data;
    const validationError: ValidationError[] = [];
    let category: string = "";
    for (let questionId in certificate.data) {
      const dataProp = certificate.data[questionId].config.prop;
      const question = certificate.data[questionId];

      category = question.config.component === 'category' ? questionId : category;

      if (question.visible && question.validation && question.validation.required) {
        switch (question.value.type) {
          case "BOOLEAN":
            const booleanValue: CertificateBooleanValue = question.value as CertificateBooleanValue;
            if (booleanValue.selected === undefined || booleanValue.selected === null) {
              validationError.push({
                id: questionId,
                category: getCategory(certificate, question.parent),
                field: dataProp,
                type: "EMPTY",
                text: "Välj ett alternativ."
              });
            }
            break;
          case "TEXT":
            const textValue: CertificateTextValue = question.value as CertificateTextValue;
            if (!textValue.text) {
              validationError.push({
                id: questionId,
                category: getCategory(certificate, question.parent),
                field: dataProp,
                type: "EMPTY",
                text: "Ange ett svar."
              });
            }
            break;
          default:
            break;
        }
      }
    }
    return {data: validationError};
  }
  else return {};
};

function getCategory(certificate: Certificate, parent: string): string {
  if (parent) {
    const newParent = certificate.data[parent].parent;
    if (newParent) {
      return getCategory(certificate, newParent);
    }
  }
  return parent;
}

export default api;
