#!/usr/bin/env bash
source ./setenv.sh client

cd "${ABSPATH2CLASSES}"

jackc="/home/bitor/projects/redwars/dependencies/jackson-core-2.13.2.jar"
jackd="/home/bitor/projects/redwars/dependencies/jackson-databind-2.13.2.2.jar"
rabbit="/home/bitor/projects/redwars/dependencies/amqp-client-5.17.0.jar"
slf="/home/bitor/projects/redwars/dependencies/slf4j-api-2.0.7.jar"
slfnop="/home/bitor/projects/redwars/dependencies/slf4j-nop-2.0.7.jar"


java -cp ${CLASSPATH}":"${jackc}":"${jackd}":"${rabbit}":"${slf}\
     -Djava.security.policy="${CLIENT_SECURITY_POLICY}" \
     -Djava.rmi.server.codebase="${SERVER_CODEBASE}" \
     -D${JAVAPACKAGEROLE}.codebase="${CLIENT_CODEBASE}" \
     -D${JAVAPACKAGE}.servicename="${SERVICE_NAME_ON_REGISTRY}" \
     "${JAVAPACKAGEROLE}"."${CLIENT_CLASS_PREFIX}""${CLIENT_CLASS_POSTFIX}" "${REGISTRY_HOST}" "${REGISTRY_PORT}" "${SERVICE_NAME_ON_REGISTRY}"


# cd "${ABSPATH2SRC}"/"${JAVASCRIPTSPATH}"
 
