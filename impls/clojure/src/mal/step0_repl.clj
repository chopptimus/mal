(ns mal.step0-repl
  (:gen-class)
  (:require [mal.main :as main]))

(defn -main
  []
  (main/repl "user> "))
