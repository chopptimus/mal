(ns mal.main
  (:require [mal.main :as main]
            [mal.printer :as printer]
            [mal.reader :as reader]))

(defn repl
  [prompt]
  (loop []
    (print prompt)
    (flush)
    (when-some [s (read-line)]
      (println (try
                 (printer/pr-str (reader/read s))
                 (catch Exception e (str e))))
      (flush)
      (recur))))
