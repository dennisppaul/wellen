# Wellen + SAM

## build instructions

### MacOS

- install *JDK 15* from [Java SE Development Kit 15 Downloads](https://www.oracle.com/java/technologies/javase-jdk15-downloads.html)
- install *CMake* via *Homebrew* ( e.g `brew install cmake` )
- if necessary set `JAVA_HOME_USR` in `CMakeLists.txt` to java home ( e.g `set(JAVA_HOME_USR /Library/Java/JavaVirtualMachines/jdk-15.0.1.jdk/Contents/Home)` )
- run `build-jni-lib.sh`

### Linux

- update *apt*: `sudo apt-get update`
- install *CMake*, *JDK 14* and *clang*: `sudo apt-get install cmake  openjdk-14-jdk-headless clang`
- if necessary set `JAVA_HOME_USR` in `CMakeLists.txt` to java home ( e.g `set(JAVA_HOME_USR /usr/lib/jvm/java-14-openjdk-amd64/)` )
- run `build-jni-lib.sh`