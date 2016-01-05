(ns spreadsheet.util
  (:require [spreadsheet.parser.parser :as parser]
            [spreadsheet.parser.helpers :as helpers]))

(defn map-formula
  "Applies a function to all of the cells (ignoring ranges) in a spreadsheet formula
   The function takes a string of length 1 and an integer as the name of each
   cell (cell A5 goes to ['A' 5])
   Example: 'A1 + B1' -> 'A2 + B2' with function
   #(vector %1 (+ %2 1))"
  [f formula]
  (loop [built-string ""
         rest-string formula]
    (if (not (empty? rest-string))
      (let [ch (helpers/first-str rest-string)
            rst (helpers/rest-str rest-string)]
        (if (helpers/is-capital ch)
          (let [[num new-rst] (parser/take-number rst)]
            (if (not (nil? num))
              (let [[new-ch new-num] (f ch (:number num))] ; apply function to cell
                (recur (str built-string new-ch new-num) new-rst))
              (recur (str built-string ch) new-rst)))
          (recur (str built-string ch) rst)))
      built-string)))
