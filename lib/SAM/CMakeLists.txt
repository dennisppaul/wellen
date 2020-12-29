cmake_minimum_required(VERSION 3.0)
project(jni_wellen_sam)

#set(JAVA_HOME_USR "set to location of Java SDK")

# ----------------------------------------------------------------------------------------

set(CMAKE_BUILD_TYPE Release)

IF (WIN32)
    message("+++ windows is not supported")
ENDIF (WIN32)

IF(NOT JAVA_HOME_USR)
    IF (UNIX)
        set(JAVA_HOME_USR /usr/lib/jvm/java-14-openjdk-amd64/)
    ENDIF (UNIX)
    IF (APPLE)
        set(JAVA_HOME_USR /Library/Java/JavaVirtualMachines/jdk-15.0.1.jdk/Contents/Home)
    ENDIF (APPLE)
ENDIF(NOT JAVA_HOME_USR)

#@TODO(check on other platforms)
#IF (APPLE)
#    message("+++ only tested on MacOS 11")
#ELSE (APPLE)
#    message("+++ not tested on Linux or Windows")
#ENDIF (APPLE)

# java
message("----- Java Native Interface -----")

IF(NOT JAVA_HOME)
    IF(JAVA_HOME_USR)
        set(JAVA_HOME ${JAVA_HOME_USR})
    ELSE(JAVA_HOME_USR)
        message(FATAL_ERROR "+++ could not find JDK. point `JAVA_HOME_USR` or `JAVA_HOME` to JDK home.")
    ENDIF(JAVA_HOME_USR)
ENDIF(NOT JAVA_HOME)
message("+++ using JDK @ " ${JAVA_HOME})

find_package(Java REQUIRED)
set(JAVA_AWT_INCLUDE_PATH NotNeeded)
find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})
include_directories(./cpp)

set(SOURCE_FILES cpp/wellen_SAM_impl.cpp)

# SAM library
message("----- SAM library -----")
include_directories(./libraries/SAM/include)
set(SAM_SRC_PATH ./libraries/SAM/src)
FILE(GLOB SAM_LIB_SRC_FILES ${SAM_SRC_PATH}/*.cpp)
set(SOURCE_FILES ${SOURCE_FILES} ${SAM_LIB_SRC_FILES})

# compiler

set(CMAKE_CXX_COMPILER "/usr/bin/clang++")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++11")
#set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -stdlib=libc++ -lc++abi") # @added
add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCE_FILES})

# ----------------------------------------------------------------------------------------
