# use Cypress provided image with all dependencies included
FROM cypress/base:10
RUN node --version
RUN npm --version

WORKDIR /home/node/cypress_test

# Copy all files needed for cypress

# Application data. Do we need this?
COPY package-lock.json ./
# COPY package.json ./

# Copy Cypress tests
COPY cypress.json ./
COPY cypress ./cypress

# Avoid many lines of progress bars since we run CI
ENV CI=1

# Install Cypress and run the tests
RUN npm ci
# RUN npm install --save-dev cypress
RUN $(npm bin)/cypress verify
# Should not this be run? RUN $(npm bin)/cypress run
