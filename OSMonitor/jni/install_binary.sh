#!/bin/sh
echo Copy binary execute to assets..
mv ./armeabi/osmcore ../assets/osmcore_arm
mv ./x86/osmcore ../assets/osmcore_x86
mv ./mips/osmcore ../assets/osmcore_mips
echo Done..
