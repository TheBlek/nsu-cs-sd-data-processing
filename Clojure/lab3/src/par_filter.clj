(ns par-filter)
(def test_list (iterate inc 1))

(def partition-size 5)
(def parallel-partition-cnt 8)

(defn par-filter [f coll]
  (->>
    (partition-all partition-size coll)
    (map #(future (doall (filter f %))))
    (partition-all parallel-partition-cnt)
    (map (fn [parts] (map deref (doall parts))))
    (flatten)))



(defn heavy_even [x]
  (Thread/sleep 10)
  (even? x))

(time (doall (take 20 (filter heavy_even test_list))))
(time (doall (take 20 (par-filter heavy_even test_list))))
