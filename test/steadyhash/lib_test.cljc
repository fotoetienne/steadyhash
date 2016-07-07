(ns steadyhash.lib-test
  (:require [steadyhash.lib :as lib]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            #?(:clj [clojure.test.check.clojure-test :refer [defspec]]
               :cljs [clojure.test.check.clojure-test :refer-macros [defspec]])))

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
  (prop/for-all [n gen/nat]
    (let [p (lib/next-prime n)]
      (prime? p))))
