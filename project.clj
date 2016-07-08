(defproject steadyhash "0.2.0"
  :description "Stable Hashing implementations in Clojure[Script]"
  :url "https://github.com/fotoetienne/steadyhash"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.2"
  :jvm-opts ["-Xmx1g"]

  :dependencies []

  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                        [org.clojure/clojurescript "1.8.51"]
                        [org.clojure/test.check "0.9.0"]
                        [org.mozilla/rhino "1.7.7"]]
         :plugins [[lein-cljsbuild "1.1.3"]
                   [lein-doo "0.1.6"]]}}

  :aliases {"deploy" ["do" "clean," "deploy" "clojars"]
            "test" ["do" "clean," "test," "doo" "rhino" "test" "once"]}

  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]

  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}

  :cljsbuild {:builds
              {:test {:source-paths ["src" "test"]
                      :compiler {:output-to "target/unit-test.js"
                                 :main 'steadyhash.runner
                                 :optimizations :whitespace}}}})
