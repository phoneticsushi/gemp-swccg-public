#!/bin/bash

set -e

# compile
mvn install

# restart the app, leave the DB alone
# "build" is the name of the service which is confusing...
docker compose down

# schedule background task to open the browser
(sleep 15; xdg-open http://localhost:17001/gemp-swccg/) &

# deliberately not -d so we can see logs and stop server easily afterwards
docker compose up
