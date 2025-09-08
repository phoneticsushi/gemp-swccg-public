#!/bin/bash

set -e

REMOTE_HOST="root@gemp.beezersin.space"

if [ "$1" == "-c" ]; then
    echo "Cleaning GEMP..."
    mvn clean
fi

echo "Building GEMP..."
mvn install

echo "Updating GEMP on $REMOTE_HOST..."
ssh $REMOTE_HOST -o RemoteCommand="cd /opt/gemp-swccg-public/src; git checkout bb25; git fetch; git reset --hard origin/bb25; git cherry-pick deploy-env"

echo "Deploying async..."
rsync -a --delete --info=progress2 gemp-swccg-async/target $REMOTE_HOST:/opt/gemp-swccg-public/src/gemp-swccg-async
echo "Deploying cards..."
rsync -a --delete --info=progress2 gemp-swccg-cards/target $REMOTE_HOST:/opt/gemp-swccg-public/src/gemp-swccg-cards
echo "Deploying common..."
rsync -a --delete --info=progress2 gemp-swccg-common/target $REMOTE_HOST:/opt/gemp-swccg-public/src/gemp-swccg-common
echo "Deploying logic..."
rsync -a --delete --info=progress2 gemp-swccg-logic/target $REMOTE_HOST:/opt/gemp-swccg-public/src/gemp-swccg-logic
echo "Deploying server..."
rsync -a --delete --info=progress2 gemp-swccg-server/target $REMOTE_HOST:/opt/gemp-swccg-public/src/gemp-swccg-server

echo "Stopping GEMP on $REMOTE_HOST..."
# "build" is the name of the service which is confusing...
ssh $REMOTE_HOST -o RemoteCommand="cd /opt/gemp-swccg-public/src; docker compose down build"

echo "Starting GEMP on $REMOTE_HOST..."
ssh "$REMOTE_HOST" -o RemoteCommand="cd /opt/gemp-swccg-public/src; docker compose up -d"

# open for test and to activate server
echo "GEMP deployed to https://gemp.beezersin.space/gemp-swccg/"
echo "Sleeping while waiting for server to restart..."
sleep 15
echo "Done!  Don't forget to open the server from the Admin Panel"

xdg-open https://gemp.beezersin.space/gemp-swccg/
