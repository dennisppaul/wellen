#! /bin/zsh

COMPILER=gcc

$COMPILER -I./include -o ../midiclock.macos ./src/midiclock.cpp ./src/rtmidi_c.cpp ./src/RtMidi.cpp -lstdc++ -std=c++11 -D__MACOSX_CORE__ -DPACKAGE_NAME="RtMidi" -DPACKAGE_TARNAME="rtmidi" -DPACKAGE_VERSION="4.0.0" -DPACKAGE_STRING="RtMidi 4.0.0" -DPACKAGE_BUGREPORT="gary@music.mcgill.ca" -DPACKAGE_URL="" -DPACKAGE="rtmidi" -DVERSION="4.0.0" -DSTDC_HEADERS=1 -DHAVE_SYS_TYPES_H=1 -DHAVE_SYS_STAT_H=1 -DHAVE_STDLIB_H=1 -DHAVE_STRING_H=1 -DHAVE_MEMORY_H=1 -DHAVE_STRINGS_H=1 -DHAVE_INTTYPES_H=1 -DHAVE_STDINT_H=1 -DHAVE_UNISTD_H=1 -DHAVE_DLFCN_H=1 -DSTDC_HEADERS=1 -DHAVE_SEMAPHORE=1 -DHAVE_LIBPTHREAD=1 -DTARGET_OS_IPHONE=0 -lpthread -framework CoreMIDI -framework CoreFoundation -framework CoreAudio
