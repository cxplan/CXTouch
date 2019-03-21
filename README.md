# CXTouch

This is an open source software which control and manage android device from PC client. This software is written in Java, so it can run on Windows, Linux and MacOS where a jre is installed (support Java 7 and later). 

You can use CXTouch for gaming, watching movie, explore app, working on mobile, and all operation supported on device, but more convenient than doing them on device without root.

CXTouch support at least 4 devices running on a pc simultaneously, the supported amount of devices depends on your actual pc performance.

![Show usage](doc/effect.gif)

## Features

- OS support
  - Desktop Client: support Windows, Linux, MacOS where a jre is installed.
  - Android: support version 4.4(SDK level 19) to 9.0(SDK Level 28)
- Control any device by mouse and keyboard
  - Real-time screen projection
    - The refresh speed depends on Android version and device performance.
    - Support rotation sensed by CXTouch automatically.
  - Support typing text from pc keyboard.
  - Supports meta keys
  - Copy string content on device and paste string content on pc to editor on device. 
- Wireless connection: The usb cable is not needed in wireless mode, but may increase image latency.
- Take a screenshot.
- Shortcut for brightness up and down.
- Shortcut for volume up and down.
- Record screen of device.
- Setting for quality and size of  image  
- Support two languages: English and Chinese.



## Requirements

- [Java](https://www.oracle.com/technetwork/java/javase/downloads/index.html) 7 and later.
- Make sure you enabled adb debugging on your device(s). 
- A addtional option should be open on some devices such as xiaomi
  - USB debugging (Security settings)
- A [ADB driver](https://adb.clockworkmod.com/) is needed if your os is Windows.



## Screenshots

[View](doc/screenshot.md)

## Latest Release

The latest version [1.2](https://github.com/cxplan/CXTouch/releases) is available.

- The package or installer for each platform has two version(with jre and without jre) except Linux (JDK is a part of Linux normally)
- The 32bit and 64bit version are supported on window os. (the 64 bit version is recommended if your windows is 64 bit.)


## Build

The project is managed by maven, so you need to install [maven](http://maven.apache.org/download.cgi) before building project.

Go to the root path of project, and run command below: 

```shell
mvn clean package -DskipTests=true
```

Then a built package will be generated in the folder ${project root}/main/target/CXTouch-*.zip

If you want to build mediate apk(CXTouch.apk), please go to the [mediate repository](https://github.com/cxplan/cxtouch_mediate) .



## Run in workspace

If you open the project using IDE, the work directory should be set: ${project root}/main, not ${project root}



## Contributing

Any contributions that make sense and respect the agreement of project are accepted forever, if you have some good advice, please tell me by information below:

Mail:  liuxiaolin425@163.com



## License

```
                   GNU GENERAL PUBLIC LICENSE
                     Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.
```



## Appreciating

Thanks Install4j for supporting building cross-platform installer which make using software convenient.

[![Install4j Web Site](https://www.ej-technologies.com/images/product_banners/install4j_small.png)](https://www.ej-technologies.com/products/install4j/overview.html)