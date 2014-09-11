diplom-thesis
======

Follow the link to the Wiki: https://github.com/normanlang/diplom/wiki/A-dynamic-signage-system-for-a-football-stadium
There you find a short abstract of my thesis and the thesis itself (in german)

Getting this code to work
=====
Requirements:
installed eclipse with maven- and git-support
For example: http://eclipse.org/downloads/packages/eclipse-ide-java-developers/keplerr

After opening eclipse:

1. Import... -> Git -> Projects from Git -> Next

2. URI -> Next

3. URI: https://github.com/normanlang/diplom.git -> Next

4. choose only branch StaticFloorField -> Next

5. set a target folder

6. after succesful download: modify buildpath -> add geomason1.5.jar and mason16.jar as extern jars (in folder lib)

7: After starting RoomWithGui.java the first time you should tell java to use more memory: -> Run Configurations... 
->  VM-arguments: -Xms1024m -Xmx1024m (if min 2 GB RAM are available) 

For more information about the architecture and to get a better understanding of the code I recommend strongly 
to read the corresponding section of my thesis and the MASON-manual found here: http://cs.gmu.edu/~eclab/projects/mason/

If you have questions to the import of georeferenced data you should also have a look at: 
http://cs.gmu.edu/~eclab/projects/mason/extensions/geomason/
