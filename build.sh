#!/bin/bash
# This is a comment

echo "node -v"
node -v

cd frontend && npm run build-prod

export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export GRAALVM_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

echo "java -version"
java -version

cd ../backend

echo "mvn -version"
./mvnw -version

./mvnw versions:set -DnextSnapshot=true

./mvnw validate

./mvnw clean -Pnative native:compile

cp target/task-stream-ai ../desktop-app/builds/task-stream-ai


cd ../desktop-app 

rm -rf release
npm run build:mac

cd ../backend

echo "Cleaning backend build target folder"
./mvnw clean

cd ..

echo "Cleaning backend build at desktop app folder"
rm -rm desktop-app/builds 