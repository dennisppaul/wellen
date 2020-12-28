cmake_minimum_required(VERSION 3.0)
project(jni_wellen_sam)
set(CMAKE_BUILD_TYPE Release)
set(JAVA_HOME_USR /Library/Java/JavaVirtualMachines/jdk-15.0.1.jdk/Contents/Home)

# ----------------------------------------------------------------------------------------

#@TODO(check on other platforms)
IF (APPLE)
    message("+++ only tested on MacOS 11")
ELSE (APPLE)
    message("+++ not tested on Linux or Windows")
ENDIF (APPLE)

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

#set(CMAKE_CXX_COMPILER "/usr/bin/clang++")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -std=c++11")
add_library(${CMAKE_PROJECT_NAME} SHARED ${SOURCE_FILES})
