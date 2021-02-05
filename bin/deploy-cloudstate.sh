#!/usr/bin/env bash
#
# Build and deploy Cloudstate to Kubernetes.
# Can be minikube, or any Kubernetes cluster by using the DOCKER_HOST env var.
#
# deploy-cloudstate.sh

set -e

echo
echo "=== Building Cloudstate docker images and deploying operator ==="
echo

[ -f docker-env.sh ] && source docker-env.sh

sbt proxy-core/docker:publishLocal

kubectl apply --validate=false -f https://github.com/jetstack/cert-manager/releases/download/v0.16.1/cert-manager.yaml
kubectl wait --for=condition=available --timeout=2m -n cert-manager deployment/cert-manager-webhook
make -C cloudstate-operator deploy
echo "Waiting for operator deployment to be ready..."
if ! kubectl wait --for=condition=available --timeout=2m -n cloudstate-system deployment/cloudstate-controller-manager
then
    kubectl describe -n cloudstate-system deployment/cloudstate-controller-manager
    kubectl describe -n cloudstate-system pods -l control-plane=controller-manager
    kubectl logs -l control-plane=controller-manager -n cloudstate-system -c manager
    exit 1
fi
