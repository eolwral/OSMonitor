#!/bin/sh
echo Copy binary execute to assets..
mv ./armeabi/osmcore ../assets/osmcore_arm
mv ./x86/osmcore ../assets/osmcore_x86
mv ./mips/osmcore ../assets/osmcore_mips
mv ./armeabi/osmcore_l ../assets/osmcore_arm_l
mv ./x86/osmcore_l ../assets/osmcore_x86_l
mv ./mips/osmcore_l ../assets/osmcore_mips_l
echo Done..
