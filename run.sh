#!/bin/bash

echo "java -cp target/classes de.oglimmer.client.Startup "$@""
java -cp target/classes de.oglimmer.client.Startup "$@"
