(ns mal.reader-test
  (:require [mal.reader :as reader]
            [clojure.test :refer [deftest is]]))

(defn string-reader [s]
  (.getBytes s))

(defn- read-one [s]
  (reader/read (reader/->pushback-reader (string-reader s))))

(defn- read-all [s]
  (let [rdr (reader/->pushback-reader (string-reader s))]
    (loop [sexps []]
      (if (reader/peek-char rdr)
        (recur (conj sexps (reader/read rdr)))
        sexps))))

(deftest symbol-test
  (is (= 'foo (read-one "foo")))
  (is (= (symbol "abc\"def") (read-one "abc\"def"))))

(deftest number-test
  (is (= 1 (read-one "1")))
  (is (= -123 (read-one "-123"))))

(deftest multiple-forms-test
  (is (= [1 -123] (read-all "1 -123")))
  (is (= '[foo bar] (read-all "foo\nbar"))))

(deftest string-test
  (is (= "stonk" (read-one "\"stonk\"")))
  (is (= "abc\"def" (read-one "\"abc\\\"def\""))))

(deftest sequences-test
  (is (= '[1 2 3 4 foo] (read-one "[1 2   3  4    foo]")))
  (is (= '(7 8 9 bar 10) (read-one "(7 8 9 bar 10)")))
  (is (= '() (read-one "(  )")))
  (is (= '(+ 1 2) (read-one "(+ 1 2 )"))))

(deftest map-test
  (is (= '{1 2} (read-one "{1 2}"))))

(deftest reader-macros
  (is (= '(quote foo) (read-one "'foo")))
  (is (= '(quasiquote foo) (read-one "`foo")))
  (is (= '(unquote foo) (read-one "~foo")))
  (is (= '(splice-unquote foo) (read-one "~@foo")))
  (is (= '(with-meta [1 2 3] {a 1}) (read-one "^{a 1} [1 2 3]"))))
