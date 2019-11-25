#!/usr/bin/env bash
set -e

docker build -t jenius/rabbitmq-operator:latest ./rabbitmq-operator-core

if [ "$TRAVIS_PULL_REQUEST" != "false" ] ; then
    echo "built pull request, nothing left to do"
elif [[ ! -z "$TRAVIS_TAG" ]]; then
    echo "tag and push docker"
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    docker tag jenius/rabbitmq-operator:latest jenius/rabbitmq-operator:$TRAVIS_TAG
    docker push jenius/rabbitmq-operator:latest
    docker push jenius/rabbitmq-operator:$TRAVIS_TAG
else
    echo "nothing to do"
fi

docker images