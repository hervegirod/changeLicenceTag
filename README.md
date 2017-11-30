# changeLicenceTag
ChangeLicenceTag is a small Java application utilities allowing to change the licence tag on a lot of Java files at once. 
It is useful if you xant to change your own licence on a lot of files

# Where is it?
The home page for the jfxConverter project can be found in the github project web site (https://github.com/hervegirod/ChangeLicenceTag). 
There you also find information on how to download the latest release as well as all the other information you might need regarding 
this project.

# Requirements
A Java 1.8 or later compatible virtual machine for your operating system.

# Installation and usage
The ChangeLicenceTag.jar file is the library of the application. Just double click on it and select:
- the new licence tag (it must include the start comment ("/*") and the end comment ("*/"). Note that the $date name will be replaced by the date of the existing licence tag for the file
- the properties file for the change
- the directory where to change the licence tags

The properties file allows to specify:
- the optional date of the change, which will be added automatically if necessary on changed files (that is, if this date is not already in the licence tag of the file)
- an optional pattern to look for the change the licence. Files which do not have this pattern i their licence tags won't be changed

# Licence
The ChangeLicenceTag Library uses a BSD license for the source code.
