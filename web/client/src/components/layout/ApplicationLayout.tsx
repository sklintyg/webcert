import React from "react";
import {Grid} from "@material-ui/core";


type Props = {
  header: React.ReactNode;
  menu: React.ReactNode;
  banner: React.ReactNode;
  body: React.ReactNode;
  footer: React.ReactNode;
};

/**
 * This is a component to use when ....
 * @param header Header....
 * @param menu
 * @param banner
 * @param body
 * @param footer
 * @constructor
 */
const ApplicationLayout: React.FC<Props> = ({header, menu, banner, body, footer}) => {
  return (
    <>
      {header}
      <Grid container direction="column">
        <Grid item>
          {menu}
        </Grid>
        <Grid item>
          {banner}
        </Grid>
        <Grid item>
          {body}
        </Grid>
        <Grid item>
          {footer}
        </Grid>
      </Grid>
    </>
  );
};

export default ApplicationLayout;
