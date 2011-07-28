# lein-condor

FIXME: write description

## Requirements

This plugin assumes that your machine is already part of a configured
Condor cluster and that your machine/user has job submission rights
to the cluster.  You can test this by following the tutorial in [the
Condor manual](http://www.cs.wisc.edu/condor/manual/v6.6/2_8Java_Applications.html).

On Ubuntu, if you'd like to try a single-node cluster, you can simply
run:

        sudo apt-get install condor

then restart your computer.  
Windows also has [simple installers available](http://www.cs.wisc.edu/condor/downloads-v2/download.pl).

## Usage

Add lein-condor to your dev-dependencies:

    (ns example
        :dev-dependencies [[lein-condor "1.0.0-SNAPSHOT"]])

Next, write the Clojure function you'd like to parallelize as the
-main method of a namespace that reads data from stdin, like so:

      (ns example.core)
      (defn -main []
            (doseq [line (line-seq (java.io.BufferedReader. *in*))]
                   (println "Hello, " line)))

Finally, place the data you'd like to process in a file within a
subdirectory of your project, and run:

             lein condor -i input.txt -o output.txt example.core

Supposing you had placed a list of names in input.txt, you'd get the
following in output.txt:

          Hello, Bill
          Hello, Charlie
          Hello, Alice

## More options

*  Specify a logfile: "-l logfile.txt "
*  Specify an error output file: "-e errors.txt"
*  Specify a platform: "-p LINUX"
*  Add command-line arguments: "lein condor [...] example.core arg1 arg2

## TODO

Allow wildcards / directories for input and output.
Allow specification of preferred remote machines.

## License

Copyright (C) 2011 Cory Giles

Distributed under the Eclipse Public License, the same as Clojure.
