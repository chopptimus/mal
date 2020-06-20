(ns mal.step1-read-print
  (:gen-class)
  (:require mal.main))

(defn -main []
  (mal.main/repl "user> "))
