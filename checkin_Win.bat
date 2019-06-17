@ECHO OFF

echo %COMPUTERNAME%
echo %PATH%

java -cp "lib/exrep.jar;lib/commons-codec-1.3.jar;lib/jdom.jar;lib/swing-layout-1.0.3.jar" common.pd.ExRepMain --client --sysout enable --logwrite enable --loglevel finer

