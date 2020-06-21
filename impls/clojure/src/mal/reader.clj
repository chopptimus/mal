(ns mal.reader
  (:refer-clojure :exclude [read] :rename {peek cpeek}))

(def ^:private pattern
  #"[ ,]*(~@|[\[\]\{\}\(\)'`~^@]|\"(?:\\.|[^\\\"])*\"?|;.*|[^\s\[\]\{\}('\"`,;)]*)")

(defn- tokenize [s]
  (filter seq (map second (re-seq pattern s))))

(defn ->reader
  [sexp]
  (atom (tokenize sexp)))

(defn peek
  [reader]
  (first @reader))

(defn next!
  [reader]
  (let [token (peek reader)]
    (swap! reader next)
    token))

(declare read-form)

(defn- read-sequence-fn
  [closer rf]
  (fn [reader]
    (loop [forms (rf)]
      (if-some [token (peek reader)]
        (if (= token closer)
          (do (next! reader) forms)
          (recur (rf forms (read-form reader))))
        (throw (ex-info "Unexpected end of input" {}))))))

(defn- list-rf
  ([] (list))
  ([x] (list x))
  ([xs x] (concat xs (list x))))

(defn- vector-rf
  ([] [])
  ([x] [x])
  ([xs x] (conj xs x)))

(def ^:private read-list (read-sequence-fn ")" list-rf))
(def ^:private read-vector (read-sequence-fn "]" vector-rf))

(def ^:private read-map-unsafe (read-sequence-fn "}" vector-rf))
(defn read-map
  [reader]
  (let [v (read-map-unsafe reader)]
    (if (odd? (count v))
      (throw
       (ex-info "Mal map literal must contain an even number of forms" {}))
      (into {} (map vec (partition 2 v))))))

(defn- read-quoted-fn
  [sym]
  (fn [reader]
    `(~sym ~(read-form reader))))

(def ^:private read-quoted (read-quoted-fn 'quote))
(def ^:private read-quasiquoted (read-quoted-fn 'quasiquote))
(def ^:private read-unquoted (read-quoted-fn 'unquote))
(def ^:private read-splice-unquoted (read-quoted-fn 'splice-unquote))
(def ^:private read-derefed (read-quoted-fn 'deref))

(defn read-form
  [reader]
  (let [token (next! reader)]
    (case token
      "(" (read-list reader) 
      "[" (read-vector reader)
      "{" (read-map reader)
      "'" (read-quoted reader)
      "`" (read-quasiquoted reader)
      "~" (read-unquoted reader)
      "@" (read-derefed reader)
      "~@" (read-splice-unquoted reader)
      (read-string token))))

(defn read [s]
  (read-form (->reader s)))
