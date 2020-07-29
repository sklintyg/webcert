import { configureStore, getDefaultMiddleware } from "@reduxjs/toolkit";
import reducer from "./reducers";
import {useDispatch} from "react-redux";
import api from "./middleware/api";
import frontendValidator from "./middleware/frontendValidator";
import backendValidator from "./middleware/backendValidator";

const store = configureStore({
  reducer,
  middleware: getDefaultMiddleware()
    .prepend(
      // correctly typed middlewares can just be used
      api,
      frontendValidator
      // ,
      // you can also manually type middlewares manually
      // untypedMiddleware as Middleware<
      //   (action: Action<'specialAction'>) => number,
      //   RootState
      //   >
    )
    // prepend and concat calls can be chained
    // .concat(logger)
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const useAppDispatch = () => useDispatch<AppDispatch>();

export default store;
