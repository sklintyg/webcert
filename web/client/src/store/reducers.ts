import { combineReducers } from "redux";
import uiReducers from "./ui";

export default combineReducers({
  ui: uiReducers,
});
