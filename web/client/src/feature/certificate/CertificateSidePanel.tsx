import React from "react";
import {Paper, Tabs, Tab, Box, Typography} from "@material-ui/core";
import {getIsShowSpinner} from "../../store/selectors/certificate";
import { useSelector } from "react-redux";

type CertificateSidePanelProp = {}

const CertificateSidePanel: React.FC<CertificateSidePanelProp> = (props) => {
  const showSpinner = useSelector(getIsShowSpinner);


  if (showSpinner) return null;

  return (
    <Box display="flex" flexDirection="column" height="100%" margin="5px">
      <Paper style={{ flexGrow: 1 }}>
        <Tabs value={0} indicatorColor="primary" textColor="primary">
          <Tab label="Om intyget" />
        </Tabs>
        <Box margin="5px">
          <Typography variant="body1">
            <Box component="span" fontWeight="fontWeightBold">Arbetsförmedlingens medicinska utlåtande</Box>
            <Box component="span" marginLeft="5px">AF00213 1.0</Box>
            <Box component="p">Arbetsförmedlingen behöver ett medicinskt utlåtande för en arbetssökande som har ett behov av fördjupat
              stöd.</Box>
            <Box component="p">Vi behöver ett utlåtande för att kunna:</Box>
            <Box>• utreda och bedöma om den arbetssökande har en funktionsnedsättning som medför nedsatt arbetsförmåga</Box>
            <Box>• bedöma om vi behöver göra anpassningar i program eller insatser</Box>
            <Box>• erbjuda lämpliga utredande, vägledande, rehabiliterande eller arbetsförberedande insatser.</Box>
          </Typography>
        </Box>
      </Paper>
    </Box>
  )
}

export default CertificateSidePanel;
