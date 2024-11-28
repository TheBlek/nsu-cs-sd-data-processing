(def test_list (take 20 (iterate inc 1)))

(def partition-size 10000)

(defn par-filter [f coll]
  (->>
    (partition-all (int (Math/ceil (Math/sqrt (count coll)))) coll)
    (map #(future (doall (filter f %))))
    (partition-all 4)
    (map (map (comp flatten (partial map deref) doall)))
    (map deref)
    (flatten)
    )
  )

(defn heavy_even [x]
  (Thread/sleep 10)
  (even? x))

;(time (reduce heavy+ 0 (range 1 16)))
;(time (p-reduce-3 heavy+ 0 (range 1 16)))

;(println (par-filter even? (take 20 (iterate inc 1))))
(time (doall (take 20 (filter heavy_even test_list))))
(time (doall (take 20 (par-filter heavy_even test_list))))