(defproject maglev-hash "0.1.0-SNAPSHOT"
  :description "Library for Consistent Hashing"
  :url "http://github.com/fotoetienne/consistent-hash"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

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

  :lein-release {:deploy-via :shell
                 :shell ["lein" "deploy"]}

  :doo {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}

  :cljsbuild {:builds
              {:test {:source-paths ["src" "test"]
                      :compiler {:output-to "target/unit-test.js"
                                 :main 'maglev-hash.runner
                                 :optimizations :whitespace}}}})
