OS Monitor for Android  [![Build Status](https://drone.io/github.com/eolwral/OSMonitor/status.png)](https://drone.io/github.com/eolwral/OSMonitor/latest)
=======

#### How to become a Beta Tester
1. [Join this community](https://plus.google.com/communities/104176911627256834500)
2. [Opt-in link](https://play.google.com/apps/testing/com.eolwral.osmonitor)

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
- German - Thanks to Benko111@XDA
- Russian - Thanks to equeim
- English
- Chinese

### Ongoing ###

- Keep logcat/dmesg as long as possible. (Working)
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

**Version 3.3.0.2**

- fix logcat function on Android L Preview
- fix "watch log" function (Thanks to Gerd)
- support 3 new screen density (experiment) 

**Version 3.1.0.1**

- Fix no data issue  (Thanks to #359)

**Version 3.1.0.0**

- German Language Files - Benko111@XDA
- Russian Language Files - equeim 
- add Disk IO and Network IO on notification (optional)
- Fix "Exit" function on KitKat
- Run background service without /bin/sh (Thanks to Andi Depressivum)
- Improve notification

**Version 3.0.9.8**

- Revert code to fix "No Data" issue

**Version 3.0.9.5**

- Fix "No Data" issue

**Version 3.0.9.3**

- Improve Whois function with own server

**Version 3.0.9.2**

- Fix incorrect swap value
- Use a new way to execute the binary

**Version 3.0.9.0**

- Fix crash and ANR issues 
- Improve Message function (Thanks to ARoiD)
   1. Able to change Logcat/Dmesg format and color
   2. Export selected log entries
   3. Search process that sent logs    
- Improve online manual
