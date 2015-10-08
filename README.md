# GameBoyLFB
## Another contender (but not really) among the many Game Boy emulators.

### Some important notes
- The internal game boy ROM file can be found here:
  - http://www.neviksti.com/DMG/DMG_ROM.bin
  - Place the above file in the root directory of the project.
- At the moment, I am only focusing on implementing support for ROM-only
  cartridges. An example is the game Alleyway (one of the GB release games).

### How to Build and Run on the Command Line

- To compile: `mvn compile`
- To compile and run tests: `mvn test`
- To run: `mvn exec:exec` (NB: this will not try to compile the project)

During the compile phase of a `compile` or `test` maven will copy the lwjgl
jar files and native runtimes to the target/lib directory and the
target/natives directory respectively.

### Using an IDE

I have tried importing the project in both Eclipse and IntelliJ IDEA. The main
thing to remember is that the target directory and libraries/natives will not
exist until a `mvn compile` phase is run. You will need to specify the path to
the lwjgl natives in your IDE run configurations as well.

Otherwise, the IDE should be able to handle everything after that.

### How to manually run after compiling
In Linux, in the base directory, the following should work:

`java -cp 'target/lib/*;target/classes' -Djava.library.path=target/natives com.github.reisnera.gameboylfb.GameBoyAppLauncher`

In Windows, do the same thing as in linux, except that the colon changes to a
semicolon, and you have to put quotes around the -D option (at least in Power-
Shell):

`java -cp 'target\lib\*;target\classes' -D'java.library.path=target\natives' com.github.reisnera.gameboylfb.GameBoyAppLauncher`

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

