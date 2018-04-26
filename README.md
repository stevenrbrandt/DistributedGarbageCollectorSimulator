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
