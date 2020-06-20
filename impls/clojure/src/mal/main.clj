(ns mal.main)

(defn repl
  [prompt]
  (loop []
    (print prompt)
    (flush)
    (when-some [s (read-line)]
      (println s)
      (flush)
      (recur))))
