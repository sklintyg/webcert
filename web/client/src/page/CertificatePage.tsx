import React from 'react';
import {useParams} from 'react-router-dom';
import {loadCertificate} from "../store/certificate/certificateSlice";
import {useDispatch} from "react-redux";
import Certificate from "../feature/certificate/Certificate";
import {Grid} from "@material-ui/core";
import {ApplicationHeader} from "../components/header/ApplicationHeader";
import {CertificateHeader} from "../feature/certificate/CertificateHeader";

type Props = {};

const CertificatePage: React.FC<Props> = () => {
  const {id} = useParams();
  const dispatch = useDispatch();

  console.log("CertificatePage", id);

  if (id) dispatch(loadCertificate(id));

  return (
    <Grid container direction="column">
      <Grid item>
        <ApplicationHeader/>
      </Grid>
      <Grid item>
        <CertificateHeader/>
      </Grid>
      <Grid item container>
        <Grid item xs={"auto"} sm={2}/>
        <Grid item xs={12} sm={8}>
          <Certificate />
        </Grid>
        <Grid item xs={"auto"} sm={2}></Grid>
      </Grid>
    </Grid>
  );
};

export default CertificatePage;
