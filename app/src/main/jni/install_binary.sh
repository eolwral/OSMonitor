#!/bin/sh
echo Copy binary execute to assets..
mkdir assets
mv libs/armeabi/osmcore assets/osmcore_arm
mv libs/x86/osmcore assets/osmcore_x86
mv libs/mips/osmcore assets/osmcore_mips
mv libs/armeabi/osmcore_pie assets/osmcore_arm_pie
mv libs/x86/osmcore_pie assets/osmcore_x86_pie
mv libs/mips/osmcore_pie assets/osmcore_mips_pie
echo Done..
