(ns mal.step1-read-print
  (:gen-class)
  (:refer-clojure :rename {read-line cread-line})
  (:require [mal.main :refer [repl]]
            [mal.reader :as reader]))

(defn no-map-comma-prn [value]
  (if (map? value)
    (do (print "{")
        (when-some [[k v] (first value)]
          (pr k)
          (print " ")
          (pr v)
          (doseq [[k v] (rest value)]
            (print " ")
            (pr k)
            (print " ")
            (pr v)))
        (println "}"))
    (prn value)))

(defn -main []
  (repl {:read-fn reader/read-line
         :eval-fn identity
         :print-fn no-map-comma-prn
         :prompt "user=> "}))
