import React from "react";
import {Paper, Tabs, Tab, Box, Typography, Link} from "@material-ui/core";
import {getIsShowSpinner} from "../../store/selectors/certificate";
import {useSelector} from "react-redux";
import SchoolIcon from '@material-ui/icons/School';
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles(() => ({
  flexContainer: {
    alignItems: "center",
    paddingTop: "20px"
  },
}))

type CertificateSidePanelProp = {
  // toggler: JSX.Element,
  // displaySidePanel: boolean
}

const CertificateSidePanel: React.FC<CertificateSidePanelProp> = (props) => {
  const showSpinner = useSelector(getIsShowSpinner);
  const styles = useStyles();

  if (showSpinner) return null;

  // if(!props.displaySidePanel){
  //   return props.toggler;
  // }

  return (
    <>
      {/*// <Box display="flex" flexDirection="column" height="100%" marginLeft={"5px"}>*/}
      {/*//   < style={{display: "flex", flexDirection: "column", height: "100%"}}>*/}
      <Tabs classes={{flexContainer: styles.flexContainer}} value={0} indicatorColor="primary" textColor="primary" style={{border: "1px solid #cdced6", alignItems: "center"}}>
        <Tab label="Om intyget"  />
        {/*{props.toggler}*/}
      </Tabs>
      <Box padding="10px" border={"1px solid #cdced6"} borderBottom={0} borderTop={0} flex={1} overflow={"auto"}>
        <Typography component={"div"} variant="body1">
          <Box component="span" fontWeight="fontWeightBold">Arbetsförmedlingens medicinska utlåtande</Box>
          <Box component="span" marginLeft="5px">AF00213 1.0</Box>
          <Box component="p">Arbetsförmedlingen behöver ett medicinskt utlåtande för en arbetssökande som har ett behov av fördjupat
            stöd.</Box>
          <Box component="p">Vi behöver ett utlåtande för att kunna:</Box>
          <Box component="p"> • utreda och bedöma om den arbetssökande har en funktionsnedsättning som medför nedsatt arbetsförmåga</Box>
          <Box component="p">• bedöma om vi behöver göra anpassningar i program eller insatser</Box>
          <Box component="p">• erbjuda lämpliga utredande, vägledande, rehabiliterande eller arbetsförberedande insatser.</Box>
        </Typography>
      </Box>
      <Box color={"#00a9a7"} border={"1px solid #cdced6"} position={"sticky"} bottom={"0"} padding={"20px"} style={{background: "white"}}>
        <Typography style={{display: "flex", alignItems: "center"}} color={"inherit"}>
          <SchoolIcon style={{marginRight: "5px"}} /><Link style={{fontSize: "14px", display: "inline-block"}} href={"#"}
                                                           color={"inherit"}> Hitta svar på dina frågor i Ineras intygsskola</Link>
        </Typography>
      </Box>
      {/*  </Paper>*/}
      {/*</Box>*/}
    </>
  )
}

export default CertificateSidePanel;
