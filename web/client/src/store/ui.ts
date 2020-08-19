import { combineReducers } from "redux";
import certificate from "./reducers/certificate";

export default combineReducers({
  uiCertificate: certificate,
});
