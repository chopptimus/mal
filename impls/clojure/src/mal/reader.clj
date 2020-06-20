(ns mal.reader
  (:refer-clojure :exclude [read] :rename {peek cpeek next cnext}))

(def ^:private pattern
  #"[ ,]*(~@|[\[\]\{\}\(\)'`~^@]|\"(?:\\.|[^\\\"])*\"?|;.*|[^\s\[\]\{\}('\"`,;)]*)")

(defn- tokenize [s]
  (filter seq (map second (re-seq pattern s))))

(defn ->reader
  [sexp]
  {:tokens (tokenize sexp)})

(defn next
  [reader]
  (update reader :tokens cnext))

(defn peek
  [reader]
  (first (:tokens reader)))

(declare read-form)

(defn- read-sequence-fn
  [closer rf]
  (fn [reader]
    (loop [reader (next reader) forms (rf)]
      (if-some [token (peek reader)]
        (if (= token closer)
          [(next reader) forms]
          (let [result (read-form reader)]
            (recur (first result) (rf forms (second result)))))
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
  (let [[reader v] (read-map-unsafe reader)]
    (if (odd? (count v))
      (throw
       (ex-info "Mal map literal must contain an even number of forms" {}))
      [reader (into {} (map vec (partition 2 v)))])))

(defn- read-atom
  [reader]
  [(next reader) (read-string (peek reader))])

(defn- read-quoted-fn
  [sym]
  (fn [reader]
    (let [[r form] (read-form (next reader))]
      [r `(~sym ~form)])))

(def ^:private read-quoted (read-quoted-fn 'quote))
(def ^:private read-quasiquoted (read-quoted-fn 'quasiquote))
(def ^:private read-unquoted (read-quoted-fn 'unquote))
(def ^:private read-splice-unquoted (read-quoted-fn 'splice-unquote))
(def ^:private read-derefed (read-quoted-fn 'deref))

(defn read-form
  [reader]
  (case (peek reader)
    "(" (read-list reader) 
    "[" (read-vector reader)
    "{" (read-map reader)
    "'" (read-quoted reader)
    "`" (read-quasiquoted reader)
    "~" (read-unquoted reader)
    "@" (read-derefed reader)
    "~@" (read-splice-unquoted reader)
    (read-atom reader)))

(defn read
  [form]
  (second (read-form (->reader form))))
