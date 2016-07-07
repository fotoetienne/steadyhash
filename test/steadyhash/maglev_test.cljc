(ns steadyhash.maglev-test
  (:require
   #?(:clj  [clojure.test :refer :all]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   [steadyhash.maglev :as m]
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

;; "Permutations for a node only list each entry once"
(defspec permutations-are-unique N
  (prop/for-all [node gen-node-id
                 m gen/pos-int]
    (let [ps (m/permutations (m/next-prime (* m 100)) node)]
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

(defspec next-prime-is-prime N
  (prop/for-all [n gen/nat]
    (let [p (m/next-prime n)]
      (prime? p))))


;; "Each node gets roughly the same allocation"
(defspec lookups-are-fair N
  (prop/for-all [nodes (gen/vector gen/uuid 2 20)]
    (let [n 1e3 ; number of lookups
          nodes (set nodes)
          table (m/populate nodes)
          lookups (map (partial m/lookup table) (range (* n (count nodes))))
          freqs (vals (frequencies lookups))
          percent-diff (-> (apply max freqs) (- n) (/ n) (* 100.0))]
      (or (< percent-diff 15)
          (println (count nodes) percent-diff)))))

;; Some lookups are currently inconsistent for cljs
;; hash is known to be consistent for strings but not integers
;; This is because js doesn't really have integers

(deftest interop-test
  (testing "clojure and javascript hash to same values"
    (let [table (m/populate [:a :b :c :d])]
      (is (= :d (m/lookup table "foo")))
      (is (= :c (m/lookup table :bar))))
    (let [table (m/populate ["a" "b" "c" "d" "e"])]
      (is (= "b" (m/lookup table "foo")))
      (is (= "e" (m/lookup table :bar)))
      ;; (is (= "d" (m/lookup table 0))) ; fails for cljs
      ;; (is (= "d" (m/lookup table 1))) ; fails for cljs
      )
    ;; (let [table (m/populate (range 100))]
    ;;   (is (= 71 (m/lookup table "foo"))) ; fails for cljs
    ;;   (is (= 81 (m/lookup table "bar")))) ; fails for cljs
    ))
