import React, { useState } from 'react';
import CertificatePage from './page/CertificatePage';
import { Route, Switch } from 'react-router-dom';
import { ThemeProvider } from '@material-ui/core/styles';
import { themeCreator } from "./components/styles/theme";
import { Switch as MuiSwitch } from '@material-ui/core';
import Brightness7Icon from '@material-ui/icons/Brightness7';
import Brightness4Icon from '@material-ui/icons/Brightness4';


function App() {
  const [darkMode, setDarkMode] = useState(false);
  const handleThemeToggle = () => {
    setDarkMode(!darkMode);
  }
  const theme = themeCreator(darkMode);

  const themeToggler = (
    <MuiSwitch checkedIcon={<Brightness4Icon />}
      icon={<Brightness7Icon />}
      checked={darkMode}
      onClick={handleThemeToggle}></MuiSwitch>);


  return (
    <ThemeProvider theme={theme}>
      <Switch>
        <Route path="/certificate/:id" render={() => <CertificatePage themeToggler={themeToggler}></CertificatePage>}></Route>
      </Switch>
    </ThemeProvider>
  );
}

export default App;
