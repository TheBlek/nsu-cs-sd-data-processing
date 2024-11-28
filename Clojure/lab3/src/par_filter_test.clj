(ns par-filter-test
  (:use par-filter)
  (:require [clojure.test :as test]))

(test/deftest parfilter-testset
  (test/testing "Testing par-filter correctness"
    (test/is (= (filter even? (take 10 (iterate inc 1))) (par-filter even? (take 10 (iterate inc 1)))))
    (test/is (= (take 10 (filter even? (iterate inc 1))) (take 10 (par-filter even? (iterate inc 1)))))
    ))