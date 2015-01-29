#!/bin/sh
echo Copy binary execute to assets..
mv ./armeabi/osmcore ../assets/osmcore_arm
mv ./x86/osmcore ../assets/osmcore_x86
mv ./mips/osmcore ../assets/osmcore_mips
mv ./armeabi/osmcore_pie ../assets/osmcore_arm_pie
mv ./x86/osmcore_pie ../assets/osmcore_x86_pie
mv ./mips/osmcore_pie ../assets/osmcore_mips_pie
echo Done..
