import { createMuiTheme } from "@material-ui/core/styles";

export const theme = createMuiTheme({
  palette: {
    primary: {
      main: "#3d4260",
    },
    secondary: {
      main: "#292f4f",
    }
  },
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
      md: 960,
      lg: 1440,
      xl: 1920,
    },
  }
});
