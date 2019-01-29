#!/bin/bash

# build multiple times
# see https://github.com/GoogleContainerTools/jib/issues/802

echo "Publishing monitoring agent to docker"

cd monitoringagent

export TAG=`if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo ${TRAVIS_BRANCH} ; fi`
mvn -q -Ddocker.tag=${TAG} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build

mvn -q -Ddocker.tag=${COMMIT} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build
mvn -q -Ddocker.tag=travis-${TRAVIS_BUILD_NUMBER} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build

cd ..
