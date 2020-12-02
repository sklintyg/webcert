# Development för webcert-testtools

## För att slippa ändra version på webcert-testtools och köra npm install vid varje ändring så behöver följande kommandon köras:

 ```sh
 cd webcertTestTools/
 npm link
 cd ..
 npm link webcert-testtools
```

Om npm link strular kan man även köra detta som en en-radare om man vill vara säker på att använda den test-tools du nyss ändrat i..
```sh
webcert/test>rm -rf node_modules/webcert-testtools && npm install && grunt
```

Då ändringar har gjorts i dessa moduler så bör man ändra versionsnummer för paketet innan incheckning
