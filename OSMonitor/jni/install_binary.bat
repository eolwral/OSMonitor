@echo Copy binary execute to assets..
@mkdir assets
@move libs\armeabi\osmcore assets\osmcore_arm
@move libs\x86\osmcore assets\osmcore_x86
@move libs\mips\osmcore assets\osmcore_mips
@move libs\armeabi\osmcore_pie assets\osmcore_arm_pie
@move libs\x86\osmcore_pie assets\osmcore_x86_pie
@move libs\mips\osmcore_pie assets\osmcore_mips_pie
@echo Done..

