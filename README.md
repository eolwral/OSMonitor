OS Monitor for Android 
=======

OS Monitor is a tool for monitoring your Android system.

It offers the following information.

- Processes - monitor all processes.
- Connections - display every network connection.
- Misc - monitor processors, network interfaces and file system.
- Messages - search dmesg or logcat in real-time. 

if you would like to know that how it works, please check Wiki page!

### Ongoing ###

- Preparing to upgrade table version.
- Keep logcat/dmesg as long as possible. 
- Offer a feature to monitor and save data for long term.

### Developed By ###

* eolwral@gmail.com

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

