(ns steadyhash.runner
  "A stub namespace to run cljs tests using doo"

(:require [doo.runner :refer-macros [doo-tests]]
          [steadyhash.lib-test]
          [steadyhash.maglev-test]
          [steadyhash.rendezvous-test]))

(doo-tests 'steadyhash.lib-test
           'steadyhash.maglev-test
           'steadyhash.rendezvous-test)
