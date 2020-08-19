import * as React from 'react';
import {useSelector} from "react-redux";
import {Grid, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import {getCertificateMetaData} from "../../store/selectors/certificate";

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
export const CertificateHeader: React.FC = props => {
  const certificateMetadata = useSelector(getCertificateMetaData);

  const styles = useStyles();

  if (!certificateMetadata) {
    return null;
  }

  return (
    <Grid container className={styles.root}>
      <Grid item xs={"auto"} sm={2}/>
      <Grid item xs={12} sm={8}>
        <Typography variant="h5" className={styles.heading}>
          {certificateMetadata.certificateName}
        </Typography>
      </Grid>
      <Grid item xs={"auto"} sm={2}/>
    </Grid>
  );
};
