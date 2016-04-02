(ns spreadsheet.parser.parser
  (:require [spreadsheet.parser.helpers :as helpers]))

;;; Tokenizer - Splits a string into an AST

;; Tokens

;; AST Token Types:
;; - numbers {:number 10}
;; - operators {:operator "+"}
;; - decimal {:decimal 10.5}
;; - strings {:string "Hello"}
;; - variables {:cell "A5"}
;; - functions {:function "SUM" [parameters]}

;; Higher precedence has lower take-precedence
(defonce take-precedence {:function 1
                          :cell 2
                          :operator 3
                          :decimal 4
                          :number 5
                          :string 6})

(defn take-number
  "Takes a number from a formula
  and returns the number and the rest of the string
  or nil if the string cannot take a number
  Example: '100 AD' -> [{:number 100} ' AD']"
  [s]
  (loop [num-str ""
         s s]
    (let [ch (helpers/first-str s)]
      (if (helpers/is-digit ch)
        (recur (str num-str ch) (helpers/rest-str s))
        [{:number (helpers/str-to-int num-str)} s]))))

(defn take-decimal
  "Takes a decimal from a formula
  and returns the decimal and the rest of the string
  or nil if the string cannot take a number
  Example: '10.0+3' -> [{:decimal 10.0} '+3']"
  [s])

(defn take-string
  "Takes a string from a formula or
  nil if the string cannot take a string"
  [s])

(defn take-cell
  "Takes a cell from a formula or
  nil if the string cannot take a cell"
  [s])

(defn take-function
  "Takes a function from a formula or
  nil if the string cannot take a formula"
  [s])
