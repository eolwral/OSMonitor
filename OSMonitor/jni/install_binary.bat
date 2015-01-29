@echo Copy binary execute to assets..
@move .\armeabi\osmcore ..\assets\osmcore_arm
@move .\x86\osmcore ..\assets\osmcore_x86
@move .\mips\osmcore ..\assets\osmcore_mips
@move .\armeabi\osmcore_pie ..\assets\osmcore_arm_pie
@move .\x86\osmcore_pie ..\assets\osmcore_x86_pie
@move .\mips\osmcore_pie ..\assets\osmcore_mips_pie
@echo Done..

