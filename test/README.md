
### För att slippa ändra version på webcert-testtools och köra npm install vid varje ändring så behöver följande kommandon köras:

 ```sh
 cd webcertTestTools/
 npm link
 cd ..
 npm link webcert-testtools
```

### Då ändringar har gjorts i dessa moduler så bör man ändra versionsnummer för paketet innan incheckning