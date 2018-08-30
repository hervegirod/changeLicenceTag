# changeLicenceTag
ChangeLicenceTag is a small Java application utilities allowing to change the licence tag on a lot of Java files at once.
It is useful if you want to change your own licence on a lot of files

# Where is it?
The home page for the ChangeLicenceTag project can be found in the github project web site (https://github.com/hervegirod/ChangeLicenceTag).
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
- "date": the optional date of the change, which will be added automatically if necessary on changed files (that is, if this date is not already in the licence tag of the file)
- "filter": an optional pattern to look for the change the licence. Files which do not have this pattern in their licence tags won't be changed
- "filterCopyrightOnly": an optional pattern to look for copyright holder names. Files which have copyright holder names different from this one will not be changed
- "filterCopyrightOnlySkip": an optional pattern to look for the "filterCopyrightOnly". Lines which do habe this pattern will not be considered for the
  "filterCopyrightOnly"

Note that the filters won't take the case into account for their checks.

# Basic Example
For the following properties file:
  filter=GNU General Public License
  date=2017

And the new license being a LGPL license text.

Only files with a "GNU General Public License" tag will be changed to LGPL. The date will be updated with the 2007 date.

# Complete Example
For the following properties file:
  filter=GNU General Public License
  filterCopyrightOnlySkip=do not alter or remove
  date=2017

And the new license being a LGPL license text.

Only files with a "GNU General Public License" tag will be changed to LGPL. The date will be updated with the 2007 date.
Files with a copyright holder different from Dassault Aviation will not be considered (the "filterCopyrightOnlySkip" allows not to look for a copyright holder
in the line if the file is the general "DO NOT ALTER OR REMOVE..." in license tags.

# Licence
The ChangeLicenceTag Library uses a BSD license for the source code.
