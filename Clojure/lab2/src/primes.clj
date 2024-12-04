(ns primes)

(def sieves
  (lazy-cat (list (iterate inc 2))
            (map (fn [sieve] (remove (fn [el] (zero? (rem el (first sieve)))) sieve)) sieves)
            )
  )

(def primes (map first sieves))

;(defn primesHelper [sieve] (
;                             cons
;                             (first sieve)
;                             (lazy-seq (primesHelper (remove (fn [el] (zero? (rem el (first sieve)))) sieve))))
;  )

(println (take 8 (nth sieves 0)))
(println (take 8 (nth sieves 1)))
(println (take 8 (nth sieves 2)))
(println (take 8 (nth sieves 3)))
(println (take 8 (nth sieves 4)))
(println (take 8 (nth sieves 5)))
(println (take 5000 primes))
