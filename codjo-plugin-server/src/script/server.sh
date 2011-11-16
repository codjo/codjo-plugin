#!/bin/sh
#
# Script de demarrage/arret d'un serveur ${artifactId}
#

${preScript}

SCRIPT_DIRECTORY=`dirname $0`
SCRIPT_PATH=`pwd`

if [ "$SCRIPT_DIRECTORY" != "." ]
then
	SCRIPT_PATH=`pwd`/$SCRIPT_DIRECTORY
fi

cd $SCRIPT_DIRECTORY

SERVER_NAME=${artifactId}
LOG_DIR=${logDir}
MAIN_CLASS=${serverMainClass}
tmp_dir=/tmp/${SERVER_NAME}

LOG_FILE=${LOG_DIR}/server-java.log

DEFAULT_ARG="-configuration ./server-config.properties"

PIDS=`/usr/ucb/ps auxwww | grep ${SERVER_NAME} | grep -v grep | gawk '{print $2}'`

# Pour demarrer un process
callJava() {
        java ${JAVA_SERVER_OPTS} ${jvmArguments} -server -Djava.io.tmpdir=${tmp_dir}/ -Dfile.encoding=windows-1252 -Dlog.dir=$LOG_DIR -Dlog.filename=server.log -cp $MY_CLASSPATH $1 $2 >> ${LOG_FILE} 2>&1 &
        SERVER_PID=$!
        sleep 10
        if [ "`/usr/ucb/ps ${SERVER_PID} | grep ${SERVER_PID}`" = "" ]
        then
            echo "The server could not be started."
            exit 1
        else
            echo "The server was successfully started."
            exit 0
        fi
}

# Pour supprimer le répertoire temporaire
destroy_application_tmp_directory() {
	/bin/rm -rf ${1}
	code_ret_rm=${?}
	if [ "${code_ret_rm}" -ne 0 ]
	then
		echo "The temporary directory cant be removed"
		echo "Error code: ${code_ret_rm}"
		exit 11
	fi
}

# Procedure pour tuer des process
killServer() {
        [ "$PIDS" != "" ] && kill $1 $PIDS >/dev/null 2>&1
}

case "$1" in
  'start')
        if [ ! -z "${PIDS}" ]
        then
            echo "Server already started. Impossible to start it again. Exiting"
            exit 10
        fi

        echo "Starting server..."
        touch ${LOG_FILE}
        mv ${LOG_FILE} ${LOG_FILE}.old

	    #Création du fichier temporaire dans /tmp
        if [ -d "${tmp_dir}" ]
        then
        	destroy_application_tmp_directory ${tmp_dir}
        fi
        mkdir ${tmp_dir}
        code_ret_mkdir=${?}
        if [ "${code_ret_mkdir}" -ne 0 ]
        then
        	echo "Creation of temporary directory has failed"
        	echo " Error code: ${code_ret_mkdir}"
        	exit 12
        fi

        set MY_CLASSPATH=""
        for i in `ls *.jar`
        do
            MY_CLASSPATH=$i:$MY_CLASSPATH
        done

        callJava $MAIN_CLASS "${DEFAULT_ARG}"
        ;;

  'stop')
        echo "Stopping server..."
        killServer -15
        ;;

  'fstop')
        echo "Killing server"
        killServer -9
        ;;
  'status')
        if [ -z "${PIDS}" ]
        then
                echo "Status server : OFF"
                exit 0
        else
                echo "Status server : ON"
                exit 1
        fi
        ;;
*)
        echo "Usage: $0 { start | stop | fstop | status}"
        ;;
esac
exit 0
