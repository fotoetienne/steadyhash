(ns maglev-hash.core-test
  (:require [clojure.test :refer :all]
            [maglev-hash.core :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(def gen-node-id gen/uuid)
(def gen-nodes (gen/not-empty (gen/vector gen-node-id 1 11)))

(defspec permutations-are-unique
  ;; "Nodes can be listed in any order"
  100 ;; the number of iterations for test.check to test
  (prop/for-all [node gen-node-id
                 m gen/pos-int]
    (let [ps (permutations (next-prime (* m 100)) node)]
      (= (count ps)
         (count (set ps))))))

(defspec populate-is-commutative
  ;; "Nodes can be listed in any order"
  10 ;; the number of iterations for test.check to test
  (prop/for-all [nodes gen-nodes]
    (= (populate nodes)
       (populate (shuffle nodes)))))

(defspec populate-is-fair
  ;; "Each node gets roughly the same allocation"
  100 ;; the number of iterations for test.check to test
  (prop/for-all [nodes gen-nodes]
    (let [nodes (set nodes)
          n (count nodes)
          m (next-prime (* n 100))
          avg (/ m n)
          table (populate nodes m)
          freqs (vals (frequencies table))
          percent-diff (-> (apply max freqs) (- avg) (/ avg) (* 100.0))]
      (or (< percent-diff 1)
          (println nodes (frequencies table) avg percent-diff)))))

(defn- prime? [n]
  (cond
    (= 2 n) true
    (even? n) false
    :else (let [root (num (int (Math/sqrt n)))]
            (loop [i 3]
              (if (> i root) true
                  (if (zero? (mod n i)) false
                      (recur (+ i 2))))))))

(defspec next-prime-is-prime
  100
  (prop/for-all [n gen/nat]
    (let [p (next-prime n)]
     (prime? p))))

(def N 1e3)
(defspec lookups-are-fair
  ;; "Each node gets roughly the same allocation"
  10 ;; the number of iterations for test.check to test
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [nodes (set nodes)
          table (populate nodes)
          lookups (map (partial lookup table) (range (* N (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- N) (/ N) (* 100.0))]
      (or (< percent-diff 10)
          (println (count nodes) percent-diff)))))
