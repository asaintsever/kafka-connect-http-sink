SHELL=/bin/bash

FQIN:=asaintsever/kafkaconnect-httpsinkconnector
CONTAINER_RUNTIME:=$(shell command -v docker 2> /dev/null || echo podman)	# Use docker by default if found, else try podman

.SILENT: ;  	# No need for @
.ONESHELL: ; 	# Single shell for a target (required to properly use local variables)
.PHONY: connector-archive connector-image connector-image-push
.DEFAULT_GOAL := connector-image


connector-archive:
	echo "Building Kafka Connect connector archive ..."
	cd connector
	mvn clean package

connector-image:
	echo "Build Kafka Connect connector image ..."
	$(CONTAINER_RUNTIME) build -t $(FQIN) .

connector-image-push:
	echo "Push Kafka Connect connector image ..."
	$(CONTAINER_RUNTIME) login
	$(CONTAINER_RUNTIME) push $(FQIN)
