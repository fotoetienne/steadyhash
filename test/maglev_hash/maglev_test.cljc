(ns maglev-hash.maglev-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [maglev-hash.maglev :as m]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            #?(:clj  [clojure.test.check.clojure-test :refer [defspec]]
               :cljs [clojure.test.check.clojure-test :refer-macros [defspec]])))

(def gen-node-id gen/uuid)
(def gen-nodes (gen/not-empty (gen/vector gen-node-id 1 11)))

(def iterations
  #?(:clj 10
     :cljs 2)) ; Because javascript is dog slow

(defspec permutations-are-unique
  ;; "Nodes can be listed in any order"
  (* 10 iterations) ;; the number of iterations for test.check to test
  (prop/for-all [node gen-node-id
                 m gen/pos-int]
    (let [ps (m/permutations (m/next-prime (* m 100)) node)]
      (= (count ps)
         (count (set ps))))))

(defspec populate-is-commutative
  ;; "Nodes can be listed in any order"
  iterations ;; the number of iterations for test.check to test
  (prop/for-all [nodes gen-nodes]
    (= (m/populate nodes)
       (m/populate (shuffle nodes)))))

(defspec populate-is-fair
  ;; "Each node gets roughly the same allocation"
  (* 10 iterations) ;; the number of iterations for test.check to test
  (prop/for-all [nodes gen-nodes]
    (let [nodes (set nodes)
          n (count nodes)
          m (m/next-prime (* n 100))
          avg (/ m n)
          table (m/populate nodes m)
          freqs (vals (frequencies table))
          percent-diff (-> (apply max freqs) (- avg) (/ avg) (* 100.0))]
      (or (< percent-diff 1)
          (println nodes (frequencies table) avg percent-diff)))))

(defn sqrt [n]
  #?(:clj (-> n Math/sqrt int num)
     :cljs (.sqrt js/Math n)))

(defn- prime? [n]
  (cond
    (= 2 n) true
    (even? n) false
    :else (let [root (sqrt n)]
            (loop [i 3]
              (if (> i root) true
                  (if (zero? (mod n i)) false
                      (recur (+ i 2))))))))

(defspec next-prime-is-prime
  (* 10 iterations) ;; the number of iterations for test.check to test
  (prop/for-all [n gen/nat]
    (let [p (m/next-prime n)]
      (prime? p))))


(def N 1e3)
(defspec lookups-are-fair
  ;; "Each node gets roughly the same allocation"
  iterations ;; the number of iterations for test.check to test
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [nodes (set nodes)
          table (m/populate nodes)
          lookups (map (partial m/lookup table) (range (* N (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- N) (/ N) (* 100.0))]
      (or (< percent-diff 10)
          (println (count nodes) percent-diff)))))
