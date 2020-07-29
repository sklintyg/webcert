import * as React from 'react';
import {makeStyles} from "@material-ui/core/styles";
import {AppBar, Grid, Typography} from "@material-ui/core";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: '#3D4260',
    height: '80px',
  },
  appbar: {
    backgroundColor: '#3D4260',
    height: '80px',
  },
  title: {
    marginTop: "20px",
    marginLeft: "10px",
  },
}));

type Props = {};

export const ApplicationHeader: React.FC<Props> = props => {

  const styles = useStyles();

  return (
    <AppBar position={"static"} className={styles.appbar}>
      <Grid container className={styles.root}>
        <Grid item xs={"auto"} sm={2}/>
        <Grid item xs={12} sm={8}>
        <Typography variant="h4" className={styles.title}>
          Webcert
        </Typography>
        </Grid>
        <Grid item xs={"auto"} sm={2}/>
      </Grid>
    </AppBar>
  );
};
