(ns steadyhash.rendezvous-test
  (:require
   #?(:clj  [clojure.test :refer :all]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   [steadyhash.rendezvous :as r]
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

;; "Each node gets roughly the same allocation"
(defspec highest-random-weight-is-fair (/ N 5)
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [n 1e3 ; number of lookups
          nodes (set nodes)
          lookups (pmap (partial r/highest-random-weight nodes) (range (* n (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- n) (/ n) (* 100.0))]
      (or (< percent-diff 15)
          (println (count nodes) percent-diff)))))

;; "Nodes can be listed in any order"
(defspec populate-is-commutative N
  (prop/for-all [nodes gen-nodes]
    (= (r/populate nodes)
       (r/populate (shuffle nodes)))))

;; "Each node gets roughly the same allocation"
(defspec populate-is-fair N
  (prop/for-all [nodes gen-nodes]
    (let [nodes (set nodes)
          n (count nodes)
          m (lib/next-prime (* n 100))
          avg (/ m n)
          table (r/populate nodes m)
          freqs (vals (frequencies table))
          percent-diff (-> (apply max freqs) (- avg) (/ avg) (* 100.0))]
      (or (< percent-diff 50)
          (println nodes (frequencies table) avg percent-diff)))))

;; "Each node gets roughly the same allocation"
(defspec lookups-are-fair N
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [n 1e3 ; number of lookups
          nodes (set nodes)
          table (r/populate nodes)
          lookups (pmap (partial r/lookup table) (range (* n (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- n) (/ n) (* 100.0))]
      (or (< percent-diff 60)
          (println (count nodes) percent-diff)))))

(deftest interop-test
  (testing "clojure and javascript hash to same values"
    (let [table (r/populate [:a :b :c :d])]
      (is (= :d (r/lookup table "foo")))
      (is (= :b (r/lookup table :bar))))
    (let [table (r/populate ["a" "b" "c" "d" "e"])]
      (is (= "a" (r/lookup table "foo")))
      (is (= "a" (r/lookup table :bar)))
      (is (= "e" (r/lookup table 0)))
      (is (= "b" (r/lookup table 1))))
    (let [table (r/populate (range 100))]
      (is (= 39 (r/lookup table "foo")))
      (is (= 99 (r/lookup table "bar"))))))
