(ns leiningen.kotlinc
  "Compile Kotlin source files"
  (:require [leiningen.classpath :as classpath]
            [clojure.string :as string]
            [leiningen.core.eval :as eval]
            [leiningen.core.main :as main]
            [clojure.java.io :as io]
            [leiningen.core.project :as project])
  (:import [java.io File]))

(defn- kotlin-sources
  "Returns a lazy seq of file paths: every kotlin source file within dirs."
  [dirs]
  (for [dir dirs
        ^File source (filter #(-> ^File % (.getName) (.endsWith ".kt"))
                       (file-seq (io/file dir)))]
    (.getPath source)))

;; Tool's .run method expects the last argument to be an array of
;; strings, so that's what we'll return here.
(defn- kotlinc-options
  "Compile all sources of possible options and add important defaults.
  Result is a String java array of options."
  [project files args]
  (into-array String
    (filter #(not-empty %) (flatten ["-cp" (classpath/get-classpath-string project)
                                     "-d" (:compile-path project) "-nowarn"
                                     (:kotlinc-options project) args files]))))

;; Pure kotlin projects will not have Clojure on the classpath. As such, we need
;; to add it if it's not already there.
(def subprocess-profile
  {:dependencies [^:displace ['org.clojure/clojure (clojure-version)]]
   :eval-in :subprocess})

(defn compiler-profile
  [version]
  {:dependencies [['org.jetbrains.kotlin/kotlin-compiler version]]})

(defn- subprocess-form
  "Creates a form for running kotlinc in a subprocess."
  [compile-path files kotlinc-opts]
  (main/debug "Running kotlinc with" kotlinc-opts)
  `(do
     (binding [*out* *err*]
       (println "Compiling" ~(count files) "source file(s) to" ~compile-path))
     (.mkdirs (clojure.java.io/file ~compile-path))
     (org.jetbrains.kotlin.cli.jvm.K2JVMCompiler/main (into-array String ~kotlinc-opts))))

;; We can't really control what is printed here. We're just going to
;; allow `.run` to attach in, out, and err to the standard streams. This
;; should have the effect of compile errors being printed. kotlinc doesn't
;; actually output any compilation info unless it has to (for an error)
;; or you make it do so with `-verbose`.
(defn- run-kotlinc-subprocess
  "Run kotlinc to compile all source files in the project. The compilation is run
  in a subprocess to avoid it from adding the leiningen standalone to the
  classpath, as leiningen adds itself to the classpath through the
  bootclasspath."
  [project args]
  (let [compile-path (:compile-path project)
        files (kotlin-sources (:kotlin-source-paths project))
        kotlinc-opts (vec (kotlinc-options project files args))
        version (or (:kotlin-compiler-version project) "1.0.3")
        form (subprocess-form compile-path files kotlinc-opts)]
    (when (seq files)
      (try
        (binding [eval/*pump-in* false]
          (eval/eval-in
            (dissoc (project/merge-profiles (project/merge-profiles project [subprocess-profile])
                      [(compiler-profile version)]) :source-paths)
            form))
        (catch Exception e
          (if-let [exit-code (:exit-code (ex-data e))]
            (main/exit exit-code)
            (throw e)))))))

(defn kotlinc
  "Compile Kotlin source files

Add a :kotlin-source-paths key to project.clj to specify where to find them.
Options passed in on the command line as well as options from the :kotlinc-options
vector in project.clj will be given to the compiler; e.g. `lein kotlinc -verbose`.
Like the compile and deps tasks, this should be invoked automatically when
needed and shouldn't ever need to be run by hand."
  [project & args]
  (run-kotlinc-subprocess project args))
