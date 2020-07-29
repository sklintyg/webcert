// @flow
import * as React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {makeStyles} from "@material-ui/core/styles";
import {
  getCertificate,
  isValidationNeeded,
  showValidationError,
  validateCertificate,
  validateCertificateNew
} from "../../store/certificate/certificateSlice";
import {useCallback} from "react";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: '##fff',
    height: '80px',
    boxShadow: '0 2px 4px 0 rgba(0,0,0,.12)',
    borderBottom: '1px solid #d7d7dd',
  },
  heading: {
    marginTop: "20px",
    marginLeft: "10px"
  },
}));

type Props = {

};

const CertificateValidation: React.FC = props => {
  const isShowValidationError = useSelector(showValidationError);
  const validationNeeded = useSelector(isValidationNeeded);

  const dispatch = useDispatch();

  const styles = useStyles();

  const dispatcher = useCallback((action) => dispatch(action), [dispatch]);

  if (validationNeeded) {
    dispatcher(validateCertificateNew());
  }

  return null;
}

export default CertificateValidation;
