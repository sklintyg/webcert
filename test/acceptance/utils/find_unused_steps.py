#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import re

# regex = re.compile(r'this\.Given\(\/\^', re.I)

steps = []

regex = re.compile(r'this\.Given\(\/\^(.*?)\$\/', re.MULTILINE)
for root, dirs, files in os.walk("../features/"):
    for file in files:
        if file.endswith(".js"):
            path = os.path.join(root, file)
            with open(path) as f:
                # print 'DEBUG: path %s' % path
                contents = f.read()
                matches = regex.findall(contents)
                for match in matches:
                    steps.append([match.decode('utf8'), f.name])  # [step,foundInFile]

print 'Unused steps:'
for step in steps:
    hasFound = 0
    step_regex = r"" + step[0]
    regex = re.compile(step_regex, re.MULTILINE)

    # print 'checking match for %s' % step
    for root, dirs, files in os.walk("../features/"):
        for file in files:
            if file.endswith(".feature"):
                path = os.path.join(root, file)
                with open(path) as f:
                    # print 'DEBUG: path %s' % path
                    contents = f.read().decode('utf8')
                    matches = regex.findall(contents)
                    if matches:
                        hasFound = 1

    if hasFound is 0:
        print '%s:%s' % (step[1], step[0])
