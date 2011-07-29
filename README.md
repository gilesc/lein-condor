# lein-condor

Parallelize Clojure functions on a Condor cluster.

## Requirements

This plugin assumes that your machine is already part of a configured
Condor cluster and that your machine/user has job submission rights to
the cluster.  You can test this by following the tutorial in [the
Condor
manual](http://www.cs.wisc.edu/condor/manual/v6.6/2_8Java_Applications.html).

On Ubuntu, if you'd like to try a single-node cluster, you can simply
run:

        sudo apt-get install condor

then restart your computer.  Windows also has [simple installers
available](http://www.cs.wisc.edu/condor/downloads-v2/download.pl).

## Usage

Add lein-condor to your dev-dependencies:

    (ns example
        :dev-dependencies [[lein-condor "1.0.1-SNAPSHOT"]])

Next, write the Clojure function you'd like to parallelize as the
-main method of a namespace that reads data from stdin, like so
(gen-class is also required):

      (ns example.core
          (:gen-class))
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

## Batch runs

Since the purpose of using a cluster is after all to parallelize, you
may want to run more than one job at a time.  To accomplish this, you
can supply *directories* as arguments to -i and -o.  More concretely,
suppose you have a directory tree like:

        data/input/aa
        data/input/ab
        data/input/ac
        data/output

Where aa, ab, and ac are files with data similar to that in input.txt
(hint: GNU split is good for this). You can then run:
    
    lein condor -i data/input -o data/output example.core

and data/output will be automagically populated with files aa.out,
ab.out, etc. Remember, though, arguments to BOTH -i and -o must be
either files or directories. Mixing the two will cause an error.
Another thing to keep in mind is that Condor's network file sharing
can get overwhelmed when you submit too many large input files in
quick succession.  If this is the case, your log file will show errors
to this effect, and you can specify a larger delay with the -d switch
(e.g., -d 5 = 5 second delay between jobs).

## More options

*  Specify a logfile: "-l logfile.txt "
*  Specify an error output file: "-e errors.txt"
*  Specify a platform: "-p LINUX"
*  Specify a delay between multiple submissions "-d 5"
*  Add command-line arguments: "lein condor [...] example.core arg1 arg2

## TODO

*  Allow specification of preferred remote machines.
*  Test harnesses.

## License

Copyright (C) 2011 Cory Giles

Distributed under the Eclipse Public License, the same as Clojure.
