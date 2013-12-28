OS Monitor for Android 
=======

OS Monitor is a tool for monitoring your Android system.

It offers the following information.

- Processes - monitor all processes.
- Connections - display every network connection.
- Misc - monitor processors, network interfaces and file system.
- Messages - search dmesg or logcat in real-time. 

if you would like to know that how it works, please check Wiki page!

**Support Languages**
- Polish - Thanks to Jarek Mazur
- Hebrew - Thanks to Zamarin Arthur
- Italian - Thanks to Gabriele Zappi
- English
- Chinese

### Ongoing ###

- Preparing to upgrade table version.
- Keep logcat/dmesg as long as possible. 
- Offer a feature to monitor and save data for long term.

### Developed By ###

* eolwral@gmail.com

### Wiki & FAQ ###
wiki - https://github.com/eolwral/OSMonitor/wiki

### License ###

    Copyright 2013 Kenney Lu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


----------

[![Donate using PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=FSDWJ92W9MBEN&lc=US&item_name=Donate%20To%20OS%20Monitor&item_number=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted "Donate using PayPal")

----------

### Change Log ###

**Version 3.0.8.0**
- Add a app icon for each connection.
- Fix sort by name
- Add layout for tablet 
- Modify web base map to static map 
- Move help files to web
- Highlight search string on message tab
- Fix the root privileges in CM11 (Thanks to Timothy Huang)
- Modify WHOIS API to smart-ip.com
- Improve performance

**Version 3.0.7.2**
- Fix icon size on large screen
- Save settings to SQLite 
- Compile with NDK r9
- Try to fix blank screen on some devices

**Version 3.0.7.0**
- Improve English translation - Thanks to Jarek Mazur
- Add Polish language - Thanks to Jarek Mazur
- Add Italian language - Thanks to Gabriele Zappi
- Fix FC

**Version 3.0.6.8**
- Change Kill Mode (click kill icon, choose item and click kill icon again!)
- Fix no data on Atom CPU
- Able to set notification font color
- Add a option to keep notification on top
- Adjust notification layout
- Support ARMv7 CPU (for better performance)

**Version 3.0.6.6**
- New Herbw Language support - Thanks to Arthur.
- New customize notification
- Support MIPS CPU
- Fix native process name [ remove ')' ]
- Fix degrees symbol
- Fix map (Not Found)
- Fix export log 
- Don't filter network interface
- Fix FCs

**Version 3.0.6.5**
- Add CPU Time (on Process Tab)
- Add long click action in Tool mode
- Fix IPv6 Address
- Fix Detecting one core CPU
- Fix traffic statistics on VPN interface 
- Fix uptime errors
- Fix FCs

**Version 3.0.6.4**
- Fix FC on SetCPU (Android 2.3)
- Fix wrong number of cores

**Version 3.0.6.3**
- Fix CPU usage on multi-cores devices [Thanks to Vag Sta]

**Version 3.0.6.2**
- Fix Search (Message)
- Fix Watch Log (Process)
- Add Export when Watch Log (Process)
- Fix FCs

**Version 3.0.6.1**
- Fix FCs when SetCPU [Option]
- Service crashes (Some Devices execute onStop without calling onStart) [Notification]

**Version 3.0.6**
- Fix FCs [Message]
- Add a shortcut icon on the notification area [Notification]
- Avoid to detect battery status when screen off [Notification]
- Add "sort by name" [Process]
- Save last sort mode [Process]
- Avoid incorrect CPU usage [Process]
- Avoid background services to be killed by system [Misc]
- Add uptime [Misc]
- Add setCPU [Misc and Option]
- Better way to detect CPU [Misc]
- Fix memory display [Misc]

**Version 3.0.3~3.0.5.1**
- [Add] Monitor Battery, Temperature and Free RAM on notification bar.
- [Add] Add Exit button
- [FIX] Hug FCs

**Version 3.0.2**
- [Add] give a default file name as export file name (Thanks to Larry)
- [Modified] replace Google Map with OpenStreeMap to reduce privacy concerns (Thanks to Larry)
- [Fix] Fix FC (Thanks to Larry)


**Version 3.0.1**
- [Modified] Fix landscape mode
- [Modified] Better filter UI on Message (Thanks to Andreas)


**Version 3.0.0**
- [Removed] Set CPU Frequency
- [Removed] Don't Support Android version 1.5~2.2
- [Removed] Monitor Battery and Temperature 
- [Removed] Save Log as HTML
- [Modified] Replace JNI with Unix Socket IPC
- [Modified] Better filter on Message
- [Modified] New UI 
- [Add] Monitor Top Process on Notification Bar

