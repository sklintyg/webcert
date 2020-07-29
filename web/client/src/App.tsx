import React from 'react';
import CertificatePage from './page/CertificatePage';
import { Switch, Route } from 'react-router-dom';

function App() {
  return (
      <Switch>
        <Route path="/certificate/:id" component={CertificatePage}></Route>
      </Switch>
  );
}

export default App;
