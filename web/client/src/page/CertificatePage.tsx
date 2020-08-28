import React, {useEffect, useState} from 'react';
import {useParams} from 'react-router-dom';
import {useDispatch} from "react-redux";
import Certificate from "../feature/certificate/Certificate";
import {ApplicationHeader} from "../components/header/ApplicationHeader";
import {CertificateHeader} from "../feature/certificate/CertificateHeader";
import {getCertificate} from "../store/actions/certificates";
import {Container, Grid, Box} from '@material-ui/core';
import CertificateSidePanel from "../feature/certificate/CertificateSidePanel";
import ArrowForwardIosIcon from '@material-ui/icons/ArrowForwardIos';
import AssignmentIcon from '@material-ui/icons/Assignment';

type Props = {};

const CertificatePage: React.FC<Props> = () => {
  // const [displaySidePanel, setDisplaySidePanel] = useState(true);
  const {id} = useParams();
  const dispatch = useDispatch();


  console.log("CertificatePage", id);

  // const handleToggleSidePanel = () => {
  //   setDisplaySidePanel(!displaySidePanel);
  // }

  useEffect(() => {
    if (id) {
      dispatch(getCertificate(id));
    }
  }, [id]);

  // const toggler = displaySidePanel ? <ArrowForwardIosIcon style={{marginLeft: "auto", padding: "0 1em"}} onClick={handleToggleSidePanel} /> :
  //   (<Box style={{display: "flex", justifyContent: "flex-end", flex: 1}}>
  //     <Box style={{display: "flex", alignItems: "center", flexDirection: "column", backgroundColor: "#cdced6", padding: "2rem"}} onClick={handleToggleSidePanel}>
  //       <AssignmentIcon />
  //       <p>Om intyget</p>
  //     </Box>
  //   </Box>);

  // const leftGridSize = displaySidePanel ? 8 : 10;
  // const rightGridSize = displaySidePanel ? 4 : 2;

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
            <CertificateSidePanel  />
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};

export default CertificatePage;
