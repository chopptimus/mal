(ns mal.step0-repl
  (:gen-class)
  (:require mal.main)
  (:import (java.lang StringBuilder)))

(defn- identity-read [stream]
  (let [builder (StringBuilder.)]
    (loop [c (.read stream)]
      (if (#{-1 10} c)
        (str builder)
        (do (.append builder (char c))
            (recur (.read stream)))))))

(defn -main []
  (mal.main/repl {:read-fn identity-read
                  :eval-fn identity
                  :print-fn println
                  :prompt "user=> "}))
