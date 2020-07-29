import * as React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {getCertificateMetadata, signCertificate} from "../../store/certificate/certificateSlice";
import {Button, Grid, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import {useCallback} from "react";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: '##fff',
  },
  heading: {
    marginTop: "20px",
    marginLeft: "10px"
  },
  signButton: {
    backgroundColor: '#4c7b67',
    borderColor: '#4c7b67',
    color: '#fff'
  }
}));

type Props = {

};
export const CertificateFooter: React.FC = props => {
  const certificateMetadata = useSelector(getCertificateMetadata);

  const dispatch = useDispatch();

  const dispatcher = useCallback((action) => dispatch(action), [dispatch]);

  const styles = useStyles();

  return (
    <div className={styles.root}>
      {!certificateMetadata!.signed && <Button className={styles.signButton} variant="contained" onClick={() => {
        dispatcher(signCertificate(certificateMetadata!.certificateId))
      }}>Signera och skicka</Button>}
    </div>
  );
};
