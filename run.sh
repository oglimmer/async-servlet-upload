#!/bin/bash

TMP_FILENAME=$(mktemp)

cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 256 | head -n 60 >$TMP_FILENAME

echo "java -cp target/classes de.oglimmer.client.Startup "$TMP_FILENAME" "$@""
java -cp target/classes de.oglimmer.client.Startup "$TMP_FILENAME" "$@"
