(ns mal.reader
  (:refer-clojure :exclude [read-string] :rename {read cread})
  (:require [clojure.java.io :as io])
  (:import (java.lang StringBuilder)
           (java.io PushbackReader)))

(defn read-char
  [rdr]
  (let [c (.read ^java.io.PushbackReader rdr)]
    (if (neg? c)
      nil
      (char c))))

(defn unread
  [rdr c]
  (when c
    (.unread ^java.io.PushbackReader rdr (int c))))

(defn peek-char [rdr]
  (when-let [c (read-char rdr)]
    (unread rdr c)
    c))

(defmacro do-unread
  [rdr c]
  `(doto ~rdr (unread ~c)))

(defn ->pushback-reader
  ([] (->pushback-reader *in*))
  ([rdr] (PushbackReader. (io/reader rdr) 2)))

(declare read)

(defmacro read-sequence
  [rdr closer rf]
  `(do
     (consume-whitespace ~rdr)
     (loop [xs# []]
       (if-some [c# (read-char ~rdr)]
         (case c#
           ~closer (~rf xs#)
           (do (unread ~rdr c#)
               (recur (conj xs# (read ~rdr)))))
         (throw (ex-info "Unexpected EOF" {}))))))

(defn escaped-char [ch]
  (case ch
    \" \"
    \\ \\
    \n \newline
    (throw (ex-info "Unsupported escape sequence" {}))))

(defn read-string [rdr]
  (let [sb (StringBuilder.)]
    (loop [escaped? false]
      (let [ch (read-char rdr)]
        (if escaped?
          (do (.append sb (escaped-char ch))
              (recur false))
          (case ch
            nil (throw (ex-info "EOF while reading string" {}))
            \" (str sb)
            \\ (recur true)
            (do (.append sb ch)
                (recur false))))))))

(defn consume-whitespace [rdr]
  (when-some [c (read-char rdr)]
    (case c
      (\space \tab \,) (recur rdr)
      (unread rdr c))))

(defn make-symbol [s]
  (if (seq s)
    (symbol s)
    (throw (ex-info "Unexpected EOF" {}))))

(defn read-symbol [rdr]
  (let [sb (StringBuilder.)]
    (loop []
      (if-some [c (read-char rdr)]
        (case c
          (\space \tab \, \newline) (make-symbol (str sb))
          (\) \] \}) (do (unread rdr c)
                         (make-symbol (str sb)))
          (do (.append sb c)
              (recur)))
        (make-symbol (str sb))))))

(defn read-symbol-or-num [rdr]
  (let [c (read-char rdr)]
    (case (char c)
      (\0 \1 \2 \3 \4 \5 \6 \7 \8 \9) (cread (do-unread rdr c))
      \- (if-some [d (read-char rdr)]
           (do
             (unread rdr d)
             (case d
               (\0 \1 \2 \3 \4 \5 \6 \7 \8 \9) (cread (do-unread rdr c))
               (read-symbol (do-unread rdr c))))
           '-)
      (read-symbol (do-unread rdr c)))))

(defn read-unquote [rdr]
  (let [c (read-char rdr)]
    (if (= \@ c)
      (list 'splice-unquote (read rdr))
      (list 'unquote (read (do-unread rdr c))))))

(defn read-sexp
  [rdr]
  (if-some [c (read-char rdr)]
    (case (char c)
      \( (read-sequence rdr \) #(apply list %))
      \[ (read-sequence rdr \] vec)
      \{ (read-sequence rdr \} #(into {} (map vec (partition 2 %))))
      \' (list 'quote (read rdr))
      \` (list 'quasiquote (read rdr))
      \~ (read-unquote rdr)
      \@ (list 'deref (read rdr))
      \^ (let [meta (read rdr)]
           (list 'with-meta (read rdr) meta))
      \" (read-string rdr)
      (read-symbol-or-num (do-unread rdr c)))
    (throw (ex-info "Unexpected EOF" {}))))

(defn read [rdr]
  (consume-whitespace rdr)
  (let [sexp (read-sexp rdr)]
    (consume-whitespace rdr)
    sexp))

(comment
  (do
    (require '[clojure.string :as string])
    (require '[clojure.java.io :as io]))
  
  (with-open [rdr (->pushback-reader (.getBytes "( )"))]
    (read rdr)))
