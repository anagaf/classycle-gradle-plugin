# classycle-gradle-plugin

This is a Gradle plugin that performs Classycle analysis. It supports  both pure Java and 
Android projects.
 

Applying this plugin creates a bunch of new tasks with names constructed as 'classycle + 
BuildVariantName' (e.g. "classycleRelease", "classycleMain" etc). Each task performs the analysis
of the corresponding project build variant classes. Also a general "classycle" task is created
that depends on the build variants tasks. The resulting task graph looks like:
```
check
    classycle
        classycleMain
        classycleTest
        ...
```
## Usage

### Add Plugin to Your Project
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.anagaf:classycle-gradle-plugin:1.0.2"
  }
}

apply plugin: "com.anagaf.classycle"
```
Build script snippet for new, incubating, plugin mechanism introduced in 
Gradle 2.1:
```
plugins {
  id "com.anagaf.classycle" version "1.0.2"
}
```
### Create Classycle Definition File

config/classycleMain.txt:
```
show allResults

{package} = com.example
check absenceOfPackageCycles > 1 in ${package}.*
```
Specify Classycle definition file path for the build variants you would like to be checked.
```
classycleMain.definitionFilePath = "config/classycleMain.txt"
```
If definition file path is not specified Classycle tasks look for 
'config/classycleBuildVariantName.txt' (e.g. 'config/classycleProd.txt' for 
'prod' build variant) first and 'config/classycle.txt' then.  If definition 
file is not found no analysis is performed.

### Run the Analyzer

Concrete source set:
```
gradle classycleMain
```
All source sets:
```
gradle classycle
```
Also part of the "check" task:
```
gradle check
```
## Acknowledgments

Classycle is a Java dependency analysis library created by Franz-Josef Elmer. 
Read more about it at http://classycle.sourceforge.net/.

This plugin is based on classycle-gradle-plugin by Konrad Garus
https://github.com/konrad-garus/classycle-gradle-plugin
