import * as React from 'react';
import {useDispatch, useSelector} from "react-redux";
import {Button} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import {CertificateStatus} from "../../store/domain/certificate";
import {getCertificateMetaData, getIsValidating} from "../../store/selectors/certificate";
import {signCertificate} from "../../store/actions/certificates";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: '##fff',
    display: "flex",
    alignItems: "center"
  },
  heading: {
    marginTop: "20px",
    marginLeft: "10px"
  },
  signButton: {
    backgroundColor: '#4c7b67',
    borderColor: '#4c7b67',
    color: '#fff',
  },
  idText: {
    marginLeft: "auto",
    fontSize: theme.typography.fontSize
  }
}));

type Props = {

};

export const CertificateFooter: React.FC = props => {
  const certificateMetadata = useSelector(getCertificateMetaData);
  const isValidating = useSelector(getIsValidating);

  const dispatch = useDispatch();

  const styles = useStyles();

  if (!certificateMetadata) return null;

  return (
    <div className={styles.root}>
      {certificateMetadata.status === CertificateStatus.UNSIGNED &&
        <Button className={styles.signButton} disabled={isValidating} variant="contained" onClick={() => {
          dispatch(signCertificate())
        }}>Signera och skicka</Button>}
        <p className={styles.idText}>Intygs-ID: {certificateMetadata.certificateId}</p>
    </div>
  );
};
