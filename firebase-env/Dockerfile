FROM node:24-alpine

RUN apk add --no-cache openjdk21-jre
RUN npm install -g firebase-tools

WORKDIR /app

# Expose the ports: UI, Firestore, Auth
EXPOSE 4000 9098 9099

CMD ["sh", "-c", "firebase emulators:start --import=./data --export-on-exit --project demo-game-borrower"]