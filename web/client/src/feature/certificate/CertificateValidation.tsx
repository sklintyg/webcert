import * as React from 'react';
import {useSelector} from "react-redux";
import {makeStyles} from "@material-ui/core/styles";
import {getShowValidationErrors} from "../../store/selectors/certificate";

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
  const isShowValidationError = useSelector(getShowValidationErrors);

  const styles = useStyles();

  return null;
}

export default CertificateValidation;
