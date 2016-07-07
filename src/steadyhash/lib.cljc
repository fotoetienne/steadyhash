(ns steadyhash.lib)

(def primes "Infinite, lazy sequence of prime numbers."
  ((fn step [m n]
     (or (some-> (get m n)
           (-> (->> (reduce #(update %1 (+ %2 n) conj %2) (dissoc m n)))
             (step (inc n))))
         (-> (assoc m (* n n) (list n))
           (step (inc n))
           (->> (cons n) (lazy-seq)))))
   {} 2))

(defn next-prime
  "Return next prime number >= n"
  [n]
  (first (filter #(>= % n) primes)))

;; We need two portable hashing functions. Clojure uses murmur3 under the covers
;; We convert t0 a string first for clj/cljs compatibility involving integers
;; hashing integers is inconsistent in cljs since js doesn't really have integers
(def h1 (comp hash str))
;; A second hashing function is derived by simply double hashing
(def h2 (comp h1 h1))
