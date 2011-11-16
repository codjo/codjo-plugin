#!/bin/ksh
#--------------------------------------------------------------------------
# Copyright AGF AM / OSI / PL / EXP
#--------------------------------------------------------------------------
# Usage: Lancement du batch d'export
#--------------------------------------------------------------------------
# DESCRIPTION
#--------------------------------------------------------------------------
# Modification :
# - 29/11/2006,db,sg : Modification de la construction du classpath
#--------------------------------------------------------------------------
# Codes Retour :
#  0 : cf documentation
#--------------------------------------------------------------------------
# Fichiers utilises
#   Entree : $1 - initiateur de l'export
#            $2 - nom du fichier a exporter (se trouvera dans ${applicationDirOut})
#            $3 - date d'export (au format AAAA-MM-JJ)
#            $4 - "sans_unix2dos" ( si necessaire )
#            ... - arguments personnalises 
#
#   Sortie : $2
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
EXPORT_FILE=$2
EXPORT_DATE=$3
ARG4=$4
EXTRA=$4

if [ "" = "$ARG4" ]
then
    EXTRA=
else
    if [ "sans_unix2dos" = "$ARG4" ]
    then
        UNIX2DOS="sans_unix2dos"
        shift 4
    else
        typeset -L1 ARG4
        if [ "-" = "$ARG4" ]
        then
            shift 3
        else
            shift 4
        fi
    fi
    EXTRA=$@
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

set MY_CLASSPATH=""
for i in `ls *.jar`
do
    MY_CLASSPATH=$i:$MY_CLASSPATH
done

java ${JAVA_BATCH_OPTS} ${jvmArguments} -Djava.io.tmpdir=/tmp/ -Dfile.encoding=windows-1252 -Dlog.dir=${logDir} -Dlog.filename=export.log -cp $MY_CLASSPATH $MAIN_CLASS -initiator $INITIATOR -type broadcast -arg ${applicationDirOut}/$EXPORT_FILE -date $EXPORT_DATE -configuration ./batch-config.properties $EXTRA 2>>${logDir}/export_error.txt
JAVA_RESULT=$?

if [ $JAVA_RESULT != 0 ]
then
echo "-----------------------------------------------------------------" >> ${logDir}/export_error.txt
echo "Traces d'erreur lors du lancement du script d'export export.ksh" >> ${logDir}/export_error.txt
echo "-----------------------------------------------------------------" >> ${logDir}/export_error.txt
date >> ${logDir}/export_error.txt
echo  >> ${logDir}/export_error.txt
echo "Code erreur du lancement de l'export: $JAVA_RESULT" >> ${logDir}/export_error.txt
echo "Fichier d'export: $EXPORT_FILE" >> ${logDir}/export_error.txt
echo "Initiateur de l'export: $INITIATOR" >> ${logDir}/export_error.txt
echo >> ${logDir}/export_error.txt
echo "----------FIN DES TRACES D'EXPORT----------------------" >> ${logDir}/export_error.txt

echo "-------------------------------------------------------"
echo "Erreur lors du lancement du script d'export export.ksh "
echo "-------------------------------------------------------"
date
echo
echo "Code erreur du lancement de l'export: $JAVA_RESULT"
echo "Voir le fichier ${logDir}/export_error.txt pour plus de détails"
echo
echo "----------FIN DES TRACES D'EXPORT-----------------------"
    exit $JAVA_RESULT
fi

if [ "$UNIX2DOS" = "sans_unix2dos" ]
then
    exit 0
fi

# Conversion en format dos (on s'assure que le fichier existe)
touch ${applicationDirOut}/$EXPORT_FILE

if [ -f ${applicationDirOut}/$EXPORT_FILE.convert ]
then
    \rm -f ${applicationDirOut}/$EXPORT_FILE.convert
fi

unix2dos -ascii ${applicationDirOut}/$EXPORT_FILE ${applicationDirOut}/$EXPORT_FILE.convert > /dev/null

\rm -f ${applicationDirOut}/$EXPORT_FILE

mv ${applicationDirOut}/$EXPORT_FILE.convert ${applicationDirOut}/$EXPORT_FILE

chmod 777 ${applicationDirOut}/$EXPORT_FILE
