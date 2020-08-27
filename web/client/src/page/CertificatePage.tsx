import React from 'react';
import {useParams} from 'react-router-dom';
import {useDispatch} from "react-redux";
import Certificate from "../feature/certificate/Certificate";
import {ApplicationHeader} from "../components/header/ApplicationHeader";
import {CertificateHeader} from "../feature/certificate/CertificateHeader";
import {getCertificate} from "../store/actions/certificates";
import {Container, Grid, Box} from '@material-ui/core';
import CertificateSidePanel from "../feature/certificate/CertificateSidePanel";

type Props = {};

const CertificatePage: React.FC<Props> = () => {
  const {id} = useParams();
  const dispatch = useDispatch();

  console.log("CertificatePage", id);

  if (id) {
    dispatch(getCertificate(id));
  }
  return (
    <Box display="flex" flexDirection="column" height="100vh">
        <ApplicationHeader />
        <CertificateHeader />
      <Container style={{height: `calc(100vh - 191px`}}>
        <Grid container style={{height: "100%"}}>
          <Grid item sm={8} style={{overflowY: "auto", height: "100%"}}>
            <Certificate />
          </Grid>
          <Grid container item sm={4} style={{overflowY: "auto", height: "100%", display: "flex", flexDirection: "column"}}>
            <CertificateSidePanel />
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};

export default CertificatePage;
