# GameBoyLFB
## Another contender (but not really) among the many Game Boy emulators.

### How to Build
It's easy as pie! Unless I misconfigured the maven build...

- To compile: `mvn compile`
- To run: `mvn exec:exec`

You can also do `mvn package` to package stuff into a jar! Only gameboylfb
items will be packaged into the jar; lwjgl libraries and natives are not
included.

During the compile phase of either `mvn compile` or `mvn package`, maven will
copy the lwjgl jar files and native runtimes to the target/lib directory and
the target/natives directory respectively.

### How to manually run after compiling
Yeah about that. From the base directory, enter the 'target/classes' directory
that is generated after a `mvn compile` and do:
`java -cp ../lib/*:. -Djava.library.path=../natives/ \
     com.github.reisnera.gameboylfb.HelloWorld`

To manually run the jar is easier since it is configured with the classpath by
default. From the 'target' directory after a `mvn package`:
`java -Djava.library.path=natives -jar gameboylfb-1.0.0-alpha-SNAPSHOT.jar`

### License
 GameBoyLFB - A Java Game Boy emulator.
 Copyright (C) 2015 Alex Reisner <thearcher at gmail dot com>

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

