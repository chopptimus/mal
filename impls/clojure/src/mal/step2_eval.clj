(ns mal.step2-eval
  (:gen-class)
  (:require [mal.eval :as meval]
            [mal.main :refer [repl]]
            [mal.reader :as reader]))

(defn -main []
  (repl {:read-fn reader/read-line
         :eval-fn #(meval/eval meval/repl-env %)
         :print-fn prn
         :prompt "user=> "}))
