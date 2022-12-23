#! /bin/zsh

echo "+++ building android implementation for wellen library."

ANDROID_JAR=$HOME/Documents/Processing/android/sdk/platforms/android-30/android.jar
PROBE_CLASSNAME=wellen/
LIBRARY_JAR=wellen-android.jar
WELLEN_LIB_FOLDER=../../lib
WELLEN_P5_LIB_FOLDER=../../processing-library/wellen/library
WELLEN_JAR=../$WELLEN_LIB_FOLDER/wellen.jar

if [ -e $ANDROID_JAR ]
then
    echo "+++ found android library at:" $ANDROID_JAR
else
    echo "+++ ERROR could not find android library. look for 'android.jar' in android mode folder installed by processing."
fi

cd src
if [ -e ../$LIBRARY_JAR ]
then
    rm ../$LIBRARY_JAR
fi

echo "+++ compiling and packing library"
javac -classpath $WELLEN_JAR:$ANDROID_JAR wellen/*.java
jar cf $LIBRARY_JAR wellen/*.class
rm wellen/*.class
mv $LIBRARY_JAR ..
cd ..
cp $LIBRARY_JAR $WELLEN_LIB_FOLDER
cp $LIBRARY_JAR $WELLEN_P5_LIB_FOLDER
echo "+++ DONE"
