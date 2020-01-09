/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const ChromeLauncher = require("chrome-launcher")
const CDP = require("chrome-remote-interface")

const chromeFlags = [
  "--no-sandbox",
  "--headless",
]

const regExp = /HeadlessChrome\/(.*)/

module.exports = async () => {
  const chrome = await ChromeLauncher.launch({ chromeFlags })
  const protocol = await CDP({ port: chrome.port })
  const { product } = await protocol.Browser.getVersion()
  protocol.close()
  chrome.kill()
  return regExp.exec(product)[1]
}
