(ns spreadsheet.sheet
  (:require [spreadsheet.cell :refer [cell-component]]))

;; Views

(defn sheet-component [rows cols]
  [:div
   (for [x (range 0 rows)
         y (range 0 cols)]
     [cell-component x y])])
