#JD-GUI-GrepCode

_"JD-GUI-GrepCode"_, an extension for JD-GUI to search Java sources from [grepcode.com](http://grepcode.com).

##Description
_"JD-GUI-GrepCode"_ allow you to display or save the original source code of a CLASS or JAR file if it exists on grepcode.com.

##How to build JD-GUI-GrepCode ?
```
> ./gradlew build 
```
generate _"build/libs/jd-gui-grepcode-x.y.z.jar"_

##How to install JD-GUI-GrepCode ?
###Java
Add _"jd-gui-grepcode-x.y.z.jar"_ at the end of the class path
```
> java -classpath jd-gui-a.b.c.jar;jd-gui-grepcode-x.y.z.jar org.jd.gui.App
```
or create a folder named _"ext"_ in the JD-GUI installation folder, copy _"jd-gui-grepcode-x.y.z.jar"_ inside and launch JD-GUI.
```
> mkdir ext
> copy .../path/to/jd-gui-grepcode-x.y.z.jar ext
> java -classpath jd-gui-a.b.c.jar org.jd.gui.App
or
> java -jar jd-gui-a.b.c.jar
```
###Linux
Create the folder _"/opt/jd-gui/ext"_, copy _"jd-gui-grepcode-x.y.z.jar"_ inside and launch JD-GUI.
```
> sudo mkdir /opt/jd-gui/ext
> sudo copy .../path/to/jd-gui-grepcode-x.y.z.jar /opt/jd-gui/ext
```

###Mac OSX
On the JD-GUI application icon, click on _"Show Package Contents"_, go to _"Contents/Resources/Java"_, create a folder named _"ext"_, 
copy _"jd-gui-grepcode-x.y.z.jar"_ inside and launch JD-GUI. 

###Windows
On the installation folder, create a folder named _"ext"_, copy _"jd-gui-grepcode-x.y.z.jar"_ inside and execute _"jd.gui.exe"_. 

##How to use JD-GUI-GrepCode ?
- Open a CLASS file in JD-GUI to see the original source code, if it exists on grepcode.com.
- Click on "Save All Sources" to download the original source codes, if it exists on grepcode.com.

##How to configure JD-GUI-GrepCode ?
Open the preferences panel to: 
- activate or desactivate the extension,
- include or exclude some packages or files from the GrepCode searches.  

##How to uninstall JD-GUI ?
Delete _"jd-gui-grepcode-x.y.z.jar"_ from your file system.
