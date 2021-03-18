(ns mal.eval
  (:refer-clojure :exclude [eval resolve])
  (:import (clojure.lang Symbol
                         IPersistentList
                         IPersistentVector
                         IPersistentMap)))

(def ^:dynamic *env*)

(def repl-env {'+ #'+
               '- #'-
               '* #'*
               '/ #'/})

(defn resolve
  [env sym]
  (if-some [var (get env sym)]
    var
    (throw (ex-info "Could not resolve symbol" {:symbol sym}))))

(declare eval)

(defn eval-form
  [env form]
  (condp instance? form
    Symbol (resolve env form)
    IPersistentList (map #(eval env %) form)
    IPersistentVector (mapv #(eval env %) form)
    IPersistentMap (->> form
                        (map (fn [kv] (mapv #(eval env %) kv)))
                        (into {}))
    form))

(defn eval
  [env form]
  (cond
    (not (instance? IPersistentList form)) (eval-form env form)
    (seq form) (let [[f & args] (eval-form env form)]
                 (apply f args))
    :else '()))
