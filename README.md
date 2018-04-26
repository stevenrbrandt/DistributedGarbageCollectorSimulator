# Distributed Garbage Collector Simulator

Overview of codes and programs:
* src - Contains a full implemenation of the SWPR algorithm described in ISMM 2018.
* python
** SWPR-1.py - This is an implementation of the "single collector" described in ISMM 2018.
** built-in-tests.py - run the java code, executing all the tests in the test suite.
** file-tests.py - run the java code, executing all the tests from the tests folder.
** fit.py - Create the png files in the plots directory. See make-plots.sh for how to invoke it.
** make-plots.py - Generate the test output for all tests of a given type.
* bash
** make-plots.sh - Invoke both make-plots.py and fit.py to generate png files in plots/
** build.sh - Build the Java code.
* plots - The output of make-plots.sh. These are the figures in the ISMM 2018 paper.

Running from the command line:
* sh bash/build.sh
* java -ea -DCheckCounts=no -Dverbose=yes -Dseed=NUM -Dfileloc=filename.txt -Dtest=file-input -cp build/classes edu.lsu.cct.distgc.Main
** -ea - Assertions must be enabled
** -DCheckCounts=no - This ensures that phantom counts, etc. never go negative. This will not happen for the built-in tests, but is allowed for the test files.
** -DVerbose=yes - If one is only interested in the answer and not the intermediate steps, this setting is fine.

Syntax for the test file:
 create NODENUM
 edge NODENUM-&gt;NODENUM
 deledge NODENUM-&gt;NODENUM
