#!/usr/bin/env bash

# Requires `docco` to be installed:
#     npm install -g docco

mkdir -p docs/scala

docco -c src/main/resources/docco.css -o docs/scala `find src/main/scala -name \*.scala`
