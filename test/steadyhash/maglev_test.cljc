(ns steadyhash.maglev-test
  (:require
   #?(:clj  [clojure.test :refer :all]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   [steadyhash.maglev :as m]
   [steadyhash.lib :as lib]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop :include-macros true]
   #?(:clj [clojure.test.check.clojure-test :refer [defspec]]
      :cljs [clojure.test.check.clojure-test :refer-macros [defspec]])))

(def gen-node-id gen/uuid)
(def gen-nodes (gen/not-empty (gen/vector gen-node-id 1 11)))

(def N "Number of quickcheck iterations per test"
  #?(:clj 100
     :cljs 10)) ; Because javascript is dog slow

#?(:cljs (def pmap map))

;; "Permutations for a node only list each entry once"
(defspec permutations-are-unique N
  (prop/for-all [node gen-node-id
                 m gen/pos-int]
    (let [ps (m/permutations (lib/next-prime (* m 100)) node)]
      (= (count ps)
         (count (set ps))))))

;; "Nodes can be listed in any order"
(defspec populate-is-commutative N
  (prop/for-all [nodes gen-nodes]
    (= (m/populate nodes)
       (m/populate (shuffle nodes)))))

;; "Each node gets roughly the same allocation"
(defspec populate-is-fair N
  (prop/for-all [nodes gen-nodes]
    (let [nodes (set nodes)
          n (count nodes)
          m (lib/next-prime (* n 100))
          avg (/ m n)
          table (m/populate nodes m)
          freqs (vals (frequencies table))
          percent-diff (-> (apply max freqs) (- avg) (/ avg) (* 100.0))]
      (or (< percent-diff 1)
          (println nodes (frequencies table) avg percent-diff)))))

;; "Each node gets roughly the same allocation"
(defspec lookups-are-fair N
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [n 1e3 ; number of lookups
          nodes (set nodes)
          table (m/populate nodes)
          lookups (pmap (partial m/lookup table) (range (* n (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- n) (/ n) (* 100.0))]
      (or (< percent-diff 15)
          (println (count nodes) percent-diff)))))

(deftest interop-test
  (testing "clojure and javascript hash to same values"
    (let [table (m/populate [:a :b :c :d])]
      (is (= :d (m/lookup table "foo")))
      (is (= :a (m/lookup table :bar))))
    (let [table (m/populate ["a" "b" "c" "d" "e"])]
      (is (= "d" (m/lookup table "foo")))
      (is (= "d" (m/lookup table :bar)))
      (is (= "d" (m/lookup table 0)))
      (is (= "c" (m/lookup table 1))))
    (let [table (m/populate (range 100))]
      (is (= 80 (m/lookup table "foo")))
      (is (= 14 (m/lookup table "bar"))))))
