.PHONY: build
build: .npmrc
	npm install --workspaces=false
	npm run build
	
.PHONY: build-local
build-local:
	mkdir -p protos-js && \
		(cp -r ../../../common/protos-js/common ./protos-js/ && \
		cp -r ../../../common/protos-js/corda ./protos-js/ && \
		cp -r ../../../common/protos-js/driver ./protos-js/ && \
		cp -r ../../../common/protos-js/fabric ./protos-js/ && \
		cp -r ../../../common/protos-js/msp ./protos-js/ && \
		cp -r ../../../common/protos-js/networks ./protos-js/ && \
		cp -r ../../../common/protos-js/peer ./protos-js/ && \
		cp -r ../../../common/protos-js/relay ./protos-js/ && \
		cp -r ../../../common/protos-js/identity ./protos-js/ && \
		cp -r ../../../common/protos-js/package.json ./protos-js/) || \
		(rm -rf protos-js && echo "Error: Please build weaver/common/protos-js locally" && exit 1)
	(cp package.json package-remote.json && \
		cp package-local.json package.json && \
		npm install --workspaces=false && \
		cp -r protos-js build/) || \
		(mv package-remote.json package.json && exit 1)	# Runs if failure
	mv package-remote.json package.json	                # Runs if success
	npm run build
	

.PHONE: publish
publish: .npmrc
	npm publish --no-workspaces
	

clean:
	rm -rf build node_modules protos-js package-lock.json .nyc_output coverage
