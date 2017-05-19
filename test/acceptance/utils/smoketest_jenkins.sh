cd test/acceptance

# Tills vi når 5.3-release. Nuvarande release 5.2. försöker köra script som misslyckas från Jenkins.
git checkout origin/develop package.json

npm install
npm list webcert-testtools
npm config list

# Fler trådar för dns-uppslagning, funkar detta?
export UV_THREADPOOL_SIZE=128

# Patcha selenium-webdriver att sända om vid timeout
sed -i "s,\(e.code === 'ECONNRESET'\),\1 || e.code === 'ETIMEDOUT'," node_modules/selenium-webdriver/http/index.js

DATABASE_PASSWORD=b4pelsin ./node_modules/grunt-cli/bin/grunt acc:${intygstjanst_environment} --gridnodeinstances=5 --CI=true -tags=\"${selected_tags}\" --verbose

