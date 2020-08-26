import React from "react";
import webcertLogo from "./webcert_logo.png";
import {makeStyles} from "@material-ui/core/styles";
import {Box} from "@material-ui/core";

const useStyles = makeStyles({
  logo: {
    maxheight: 26,
    maxWidth: 121,
    margin: 20,
  },
  title: {
    backgroundColor: "#292f4f",
  }
});

type WebcertTitleProp = {
}

const WebcertTitle: React.FC<WebcertTitleProp> = (prop) => {
  const classes = useStyles();

  return (
    <Box className={classes.title}>
      <img src={webcertLogo} alt="logo" className={classes.logo}/>
    </Box>
  );
}

export default WebcertTitle;
