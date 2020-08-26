import React from 'react';
import CertificatePage from './page/CertificatePage';
import {Switch, Route} from 'react-router-dom';
import { ThemeProvider } from '@material-ui/core/styles';
import {theme} from "./components/styles/theme";

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Switch>
        <Route path="/certificate/:id" component={CertificatePage}></Route>
      </Switch>
    </ThemeProvider>
  );
}

export default App;
