# BCSAR View
BCSAR View is a WIP tool for editing 3DS Sound Archives (*.bcsar).

At the moment, it supports dumping and inserting new wave sounds, sound sets, and archives. You can also dump sequences, banks, and groups.

Since BCSAR View is a WIP tool, you should take precautions before making any edits. Always back up your BCSAR before attempting to import or insert new sounds.

## Installation
BCSAR View targets Java 8. To use it, you will need the Java 8 Runtime Environment.

Once you have the Java runtime installed, you can simply download the BCSAR View JAR and run it.

## Usage
Assuming you have an extracted 3DS romfs to work with, begin by running BCSAR View. Next, click the "Open" button from the main window and select the BCSAR you want to edit.

From there, you are free to make edits to the file. When you are done, click "Save" to overwrite or "Save As" to save to a new location. For details on specific operations like dumping, adding new sounds, etc. see the following pages:
* [Setup](https://github.com/thane98/BCSAR-View/wiki/Setup)
* [Dumping](https://github.com/thane98/BCSAR-View/wiki/Dumping/_edit)
* [Importing Sounds](https://github.com/thane98/BCSAR-View/wiki/Importing-Sounds)
* [Editing Sound Sets](https://github.com/thane98/BCSAR-View/wiki/Editing-Sound-Sets)
* [Creation](https://github.com/thane98/BCSAR-View/wiki/Creation)

## Building
First, install JDK8. OpenJDK users should also install OpenJFX8.

BCSAR View uses Gradle as its build system. Provided that you have JDK8, JavaFX, and Gradle installed, you can clone this repository and build the project using _gradle build_. Alternatively, you can use an IDE like IntelliJ.

## License
Unless explicitly stated in a file, this project is licensed under the GNU General Public License 3.0.
