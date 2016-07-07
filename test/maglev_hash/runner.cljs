(ns maglev-hash.runner
  "A stub namespace to run cljs tests using doo"

(:require [doo.runner :refer-macros [doo-tests]]
          [maglev-hash.maglev-test]))

(doo-tests 'maglev-hash.maglev-test)
