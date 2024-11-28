(ns primes-test
  (:use primes)
  (:require [clojure.test :as test]))

(test/deftest primes-testset
  (test/testing "Testing primes"
    (test/is (= 2 (nth primes 0)))
    (test/is (= 3 (nth primes 1)))
    (test/is (= 5 (nth primes 2)))
    (test/is (= 7 (nth primes 3)))
    (test/is (= 11 (nth primes 4)))
    (test/is (= 13 (nth primes 5)))
    (test/is (= 17 (nth primes 6)))
    (test/is (= 19 (nth primes 7)))

    (test/is (= 10000 (count (take 10000 primes))))
    ))