(ns leiningen.condor
  (:require
   [clojure.contrib.string :as string]
   [clojure.contrib.shell :as shell]
   [clojure.java.io :as jio])
  (:use
   [clojure.contrib.command-line :only [make-map]]
   [leiningen.jar :only [get-default-uberjar-name]]
   [leiningen.uberjar :only [uberjar]]))

(def cmdspec
  '[[input i "Input file" nil]
    [output o "Output file" nil]
    [platform p "Required platform (e.g., LINUX or WINNT61)" nil]
    [error e "Error file"]
    [log l "Log file"]
    remaining])

(defn parse-configuration [args]
  (let [config (into {}
                     (map #(vector (keyword (first %)) (second %))
                          (dissoc (make-map args cmdspec)
                                  :cmd-spec)))]
    (merge (dissoc config :remaining)
           {:main-class (first  (:remaining config))
            :args (rest (:remaining config))})))

(defmulti config-line first)

(defmethod config-line :input [[_ input-file]]
  (format "input = %s" input-file))
(defmethod config-line :output [[_ output-file]]
  (format "output = %s" output-file))
(defmethod config-line :platform [[_ platform]]
  (format "requirements = (OpSys = \"%s\")" platform))
(defmethod config-line :error [[_ error-file]]
  (format "error = %s" error-file))
(defmethod config-line :log [[_ log-file]]
  (format "log = %s" log-file))
(defmethod config-line :uberjar [[_ uberjar-file]]
  (format "jar_files = %s" uberjar-file))
(defmethod config-line :default [[_ _]]
  nil)


(defn make-jobspec [config]
  (string/join
   "\n"
   (concat
    ["universe = java"
     (str "executable = "
          (config :main-class)
          ".class")
     (str "arguments = "
          (config :main-class)
          " "
          (string/join " " (config :args)))]
    (filter identity
            (map config-line config))
    ["should_transfer_files = YES"
     "when_to_transfer_output = ON_EXIT"
     "queue"])))

(defn condor [project & args]
  (let [uberjar-name (get-default-uberjar-name project)
        config (assoc (parse-configuration args)
                 :uberjar uberjar-name)
        project (merge project
                       {:aot (conj (project :aot [])
                                   (symbol (:main-class config)))
                        :main (symbol (:main-class config))})]
    (uberjar project)
    ;;Condor requires that the main-class class file actually be in the top-level
    ;;directory (...)
    (let [class-file (str
                      (last (string/split #"\." (:main-class config)))
                      ".class")]
      (jio/copy (java.io.File.
                 (str "classes/"
                      (.replace (:main-class config) "." "/") ".class"))
                (java.io.File. class-file)))    
    (spit "condor.cfg" (make-jobspec config))
    (shell/sh "sh" "-c" "condor_submit condor.cfg")))

