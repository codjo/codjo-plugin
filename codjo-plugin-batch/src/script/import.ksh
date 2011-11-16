#!/bin/ksh
#--------------------------------------------------------------------------
# Copyright AGF AM / OSI / PL / EXP
#--------------------------------------------------------------------------
# Usage: Lancement du batch d'import
#--------------------------------------------------------------------------
# DESCRIPTION
#--------------------------------------------------------------------------
# Modification :
# - 30/06/2004,vr : Gestion correcte des accents
# - 29/11/2006,db,sg : Modification de la construction du classpath
#--------------------------------------------------------------------------
# Codes Retour :
#  0 : cf documentation
#--------------------------------------------------------------------------
# Fichiers utilises
#   Entree : $1 - initiateur de l'import
#            $2 - nom du fichier a importer (se trouvant dans ${applicationDirIn})
# -
#
#   Sortie : none
# -
#
#   Temporaires :
# -
#--------------------------------------------------------------------------

${preScript}

#--------------------------------------------------------------------------
# Pre-requis
#--------------------------------------------------------------------------
MAIN_CLASS=${batchMainClass}

INITIATOR=$1
FILE_TO_IMPORT=$2

if [ "$TYPE_ENV" != "" ]
then echo DEV
else TYPE_ENV=NOT_DEV
fi

if [ "$TYPE_ENV" = DEV ]
then DATE=''
else DATE='_'`date '+%Y%m%d_%H%M%S'`
fi

SCRIPT_DIRECTORY=`dirname $0`
SCRIPT_PATH=`pwd`

if [ "$SCRIPT_DIRECTORY" != "." ]
then
	SCRIPT_PATH=`pwd`/$SCRIPT_DIRECTORY
fi

cd $SCRIPT_DIRECTORY

#==========================================================================
#
# PROGRAMME PRINCIPAL
#
#==========================================================================
# Conversion en format unix
dos2unix -ascii ${applicationDirIn}/$FILE_TO_IMPORT ${applicationDirIn}/$FILE_TO_IMPORT.unix

\rm ${applicationDirIn}/$FILE_TO_IMPORT

mv ${applicationDirIn}/$FILE_TO_IMPORT.unix ${applicationDirIn}/$FILE_TO_IMPORT${DATE}

set MY_CLASSPATH=""
for i in `ls *.jar`
do
    MY_CLASSPATH=$i:$MY_CLASSPATH
done

java ${JAVA_BATCH_OPTS} ${jvmArguments} -Djava.io.tmpdir=/tmp/ -Dfile.encoding=windows-1252 -Dlog.dir=${logDir} -Dlog.filename=import.log -cp $MY_CLASSPATH $MAIN_CLASS -initiator $INITIATOR -type import -argument ${applicationDirIn}/$FILE_TO_IMPORT${DATE} -configuration ./batch-config.properties 2>>${logDir}/import_error.txt
JAVA_RESULT=$?

if [ $JAVA_RESULT != 0 ]
then
echo "-----------------------------------------------------------------" >> ${logDir}/import_error.txt
echo "Traces d'erreur lors du lancement du script d'import import.ksh" >> ${logDir}/import_error.txt
echo "-----------------------------------------------------------------" >> ${logDir}/import_error.txt
date >> ${logDir}/import_error.txt
echo  >> ${logDir}/import_error.txt
echo "Code erreur du lancement de l'import: $JAVA_RESULT" >> ${logDir}/import_error.txt
echo "Fichier d'import: $FILE_TO_IMPORT" >> ${logDir}/import_error.txt
echo "Initiateur de l'import: $INITIATOR" >> ${logDir}/import_error.txt
echo >> ${logDir}/import_error.txt
echo "----------FIN DES TRACES D'IMPORT----------------------" >> ${logDir}/import_error.txt

echo "-------------------------------------------------------"
echo "Erreur lors du lancement du script d'import import.ksh "
echo "-------------------------------------------------------"
date
echo
echo "Code erreur du lancement de l'import: $JAVA_RESULT"
echo "Voir le fichier ${logDir}/import_error.txt pour plus de détails"
echo
echo "----------FIN DES TRACES D'IMPORT-----------------------"
    exit $JAVA_RESULT
fi

