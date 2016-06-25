# kotlinc-lein

A Leiningen plugin for compiling Kotlin source files. It behaves similarly with the Leiningen javac plugin.

####Latest version:
[![Clojars Project](https://img.shields.io/clojars/v/kotlinc-lein.svg)](https://clojars.org/kotlinc-lein)

Include the dependency as shown above in your project.clj or `:user` of profiles.clj

## Source Layout

By default, Leiningen assumes your project only has Clojure source code under
`src`. When using both Clojure and Kotlin in the same codebase, however, it is
necessary to tell Leiningen where to find Kotlin sources.

To do so, use `:source-paths` and `:kotlin-source-path` options in the project
definition:

```clojure
(defproject megacorp/superservice "1.0.0-SNAPSHOT"
  :description "A Clojure project with a little bit of Kotlin sprinkled here and there"
  :min-lein-version    "2.0.0"
  :source-paths        ["src/clojure"]
  :kotlin-source-paths ["src/kotlin"])
```

Having one source root contain another (e.g. `src` and `src/kotlin`) can
cause obscure problems.

## Kotlin Source Compilation

To compile Kotlin sources, you can run

    $ lein kotlinc

To automatically compile Kotlin sources add `"kotlinc"` to the `:prep-tasks` vector

## Setting Kotlin Compiler Options With Leiningen

When compiling Kotlin sources, it may be necessary to pass extra arguments to the
compiler. For example, it is very important to target the version of the generated
JVM bytecodes. This can be done via the `:kotlinc-options` which takes a vector of
arguments as you would pass them to `kotlinc` on the command line. In this case we want to generate
JVM bytecode `1.6` and to use Kotlin language version `1.0`:

```clojure
(defproject megacorp/superservice "1.0.0-SNAPSHOT"
  :description "A Clojure project with a little bit of Kotlin sprinkled here and there"
  :min-lein-version    "2.0.0"
  :source-paths        ["src/clojure"]
  :kotlin-source-paths ["src/kotlin"]
  :kotlinc-options     ["-jvm-target" "1.6" "-language-version" "1.0"])
```

Equivalently through the command line:

    $ lein kotlinc -jvm-target 1.6 -language-version 1.0

Take note that the example options above are only available when using the snapshot of the
Kotlin compiler version `1.1`

## Default Kotlin Compiler Version (1.0.2)

To use different version of the Kotlin compiler, you can do it like so:

```clojure
(defproject megacorp/superservice "1.0.0-SNAPSHOT"
  :description "A Clojure project with a little bit of Kotlin sprinkled here and there"
  :min-lein-version        "2.0.0"
  :source-paths            ["src/clojure"]
  :kotlin-source-paths     ["src/kotlin"]
  :kotlin-compiler-version "1.1-SNAPSHOT"
  :repositories            [["snapshots" "http://oss.sonatype.org/content/repositories/snapshots"]]
```

## Kotlin Runtime Dependencies

`[org.jetbrains.kotlin/kotlin-runtime "1.0.2"]` should be added to `:dependencies`
for successful compilation. Make sure to match for the compiler version used.

## License

Copyright © 2016 Clyde Tan

Distributed under the Eclipse Public License either version - v 1.0.
