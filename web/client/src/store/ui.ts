import { combineReducers } from "redux";
import certificateReducer from "./certificate/certificateSlice";

export default combineReducers({
  uiCertificate: certificateReducer,
});
