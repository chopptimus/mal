(ns mal.main
  (:require [mal.main :as main]))

(defn repl
  [{:keys [prompt read-fn eval-fn print-fn]}]
  (loop []
    (print prompt)
    (flush)
    (try
      (print-fn (eval-fn (read-fn *in*)))
      (catch Exception e (println e)))
    (flush)
    (recur)))
