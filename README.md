# GameBoyLFB
## Another contender (but not really) among the many Game Boy emulators.

### Some important notes
- The internal game boy ROM file can be found here:
  - http://www.neviksti.com/DMG/DMG_ROM.bin
  - Place the above file in the root directory of the project.
- At the moment, I am only focusing on implementing support for ROM-only
  cartridges. An example is the game Alleyway (one of the GB release games).

### How to Build and Run
It's easy as pie! Unless I misconfigured the maven build...

- To compile: `mvn compile`
- To compile and test: `mvn test`
- To run: `mvn exec:exec` (NB: this will not try to compile the project)

You can also do `mvn package` to compile and package stuff into a jar! Only
gameboylfb items will be packaged into the jar; libraries and natives will not
be included.

During the compile phase of a `compile`, `test`, or `package`, maven will
copy the lwjgl jar files and native runtimes to the target/lib directory and
the target/natives directory respectively.

### How to manually run after compiling
In Linux, from the base directory, enter the 'target' directory that is
generated after a `mvn compile` and do:

`java -cp "classes:lib/*" com.github.reisnera.gameboylfb.HelloWorld`

In Windows, do the same thing as in linux, except that the colon changes to a
semicolon, and you have to put quotes around the -D option (at least in Power-
Shell):

`java -cp "classes;lib/*" com.github.reisnera.gameboylfb.HelloWorld`

To manually run the jar is easier since it is configured with the classpath by
default. From the 'target' directory after a `mvn package`:

`java -jar gameboylfb-1.0.0-alpha-SNAPSHOT.jar`

### License
 GameBoyLFB - A Java Game Boy emulator.
 Copyright (C) 2015 Alex Reisner (thearcher at gmail dot com)

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

