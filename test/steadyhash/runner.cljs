(ns steadyhash.runner
  "A stub namespace to run cljs tests using doo"

(:require [doo.runner :refer-macros [doo-tests]]
          [steadyhash.maglev-test]))

(doo-tests 'steadyhash.maglev-test)
