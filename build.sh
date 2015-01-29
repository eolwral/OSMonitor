#!/bin/bash

# environment
export ANDROID_HOME=${WORKSPACE}/android-sdk-linux 
export NDK_HOME=${WORKSPACE}/android-ndk
export COV_HOME=${WORKSPACE}/cov/bin
export PATH=${NDK_HOME}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${COV_HOME}:${PATH}

# get Android SDK
wget -q http://dl.google.com/android/android-sdk_r23.0.2-linux.tgz
tar xzf android-sdk_r23.0.2-linux.tgz

# get Android NDK
wget -q http://dl.google.com/android/ndk/android-ndk-r10c-linux-x86_64.bin
chmod u+x android-ndk-r10c-linux-x86_64.bin
./android-ndk-r10c-linux-x86_64.bin > installation.log
mv android-ndk-r10c android-ndk

# prepare build-tools
android list sdk --all --extended
echo y | android update sdk --filter tools,platform-tools,android-21,extra-android-support --no-ui --force > installation.log
echo y | android update sdk --filter build-tools-21.1.2 --no-ui --force >> installation.log

# get Coverity
wget -q https://scan.coverity.com/download/cxx/linux-64 --post-data "token=$TOKEN&project=Android+OS+Monitor" -O coverity_tool.tgz
tar zxf coverity_tool.tgz 
mv cov-analysis-linux64-7.6.0 cov 
cov-configure --comptype gcc --compiler arm-linux-androideabi-gcc --template

# get source code

# Volley
git clone https://android.googlesource.com/platform/frameworks/volley
cd volley
git checkout tags/android-5.0.0_r7
cd ..
android update lib-project --path ./volley --target android-21
echo "android.library=true" >> ./volley/project.properties

# ColorPickerPrefrence
git clone https://github.com/eolwral/android-ColorPickerPreference.git
android update lib-project --path ./android-ColorPickerPreference --target android-21

# Support Library
android update lib-project --path ./android-sdk-linux/extras/android/support/v7/appcompat/ --target android-21

# OSMonitor
android update project --path ./OSMonitor --target android-21

# clean up
cd ./OSMonitor
ant clean

# switch native binary directory
cd jni

# prepare coverity
cov-build --dir cov-int ndk-build -j4
tar czf project-coverity.tar.gz cov-int
curl --form token=$TOKEN \
  --form email=$EMAIL \
  --form file=@project-coverity.tar.gz  \
  --form version="Version" \
  --form description="Description" \
  https://scan.coverity.com/builds?project=Android+OS+Monitor

# build native binary
ndk-build
cd ..

# move file
mkdir assets
mv libs/armeabi/osmcore assets/osmcore_arm
mv libs/x86/osmcore assets/osmcore_x86
mv libs/mips/osmcore assets/osmcore_mips
mv libs/armeabi/osmcore_l assets/osmcore_arm_pie
mv libs/x86/osmcore_l assets/osmcore_x86_pie
mv libs/mips/osmcore_l assets/osmcore_mips_pie

# build debug package
ant debug
