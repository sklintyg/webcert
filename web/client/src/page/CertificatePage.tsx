import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch } from "react-redux";
import Certificate from "../feature/certificate/Certificate";
import { ApplicationHeader } from "../components/header/ApplicationHeader";
import { CertificateHeader } from "../feature/certificate/CertificateHeader";
import { getCertificate } from "../store/actions/certificates";
import { Container, Grid, Box, Paper, Switch, createMuiTheme } from '@material-ui/core';
import CertificateSidePanel from "../feature/certificate/CertificateSidePanel";


type Props = {
  themeToggler: JSX.Element
};

const CertificatePage: React.FC<Props> = (props: Props) => {
  const { id } = useParams();
  const dispatch = useDispatch();

  console.log("CertificatePage", id);


  useEffect(() => {
    if (id) {
      dispatch(getCertificate(id));
    }
  }, [id]);



  return (
    <Paper>
      <Box display="flex" flexDirection="column" height="100vh">
        <ApplicationHeader themeToggler={props.themeToggler} />
        <CertificateHeader />
        <Container style={{ height: `calc(100vh - 191px` }}>
          <Grid container style={{ height: "100%" }}>
            <Grid item sm={8} style={{ overflowY: "auto", height: "100%" }}>
              <Certificate />
            </Grid>
            <Grid container item sm={4}>
              <CertificateSidePanel />
            </Grid>
          </Grid>
        </Container>
      </Box>
    </Paper>
  );
};

export default CertificatePage;
