FROM node:16

ENV CACTUS_CMD_SOCKETIO_PATH=/opt/cactus-common-example-server

WORKDIR ${CACTUS_CMD_SOCKETIO_PATH}

# CMake is required by one of npm dependencies (install other packages in this step as well in the future)
RUN apt-get update && apt-get install -y cmake && rm -rf /var/lib/apt/lists/*

COPY ./dist/yarn.lock ./package.json ./
RUN yarn install --production --ignore-engines --non-interactive --cache-folder ./.yarnCache && \
    rm -rf ./.yarnCache

COPY ./dist ./dist
COPY ./config /etc/cactus/

EXPOSE 5034
VOLUME ["/etc/cactus/"]

CMD ["node", "./dist/src/main/typescript/routing-interface/www.js"]
