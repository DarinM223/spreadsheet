(ns spreadsheet.sheet
  (:require [spreadsheet.cell :refer [cell-component]]))

;; Views

(defn sheet-component
  "A React component for a Google Sheets style spreadsheet
   with the specified number of cells based on the rows and columns
   parameters"
  [rows cols]
  [:div
    (for [x (range 0 rows)]
      [:span
        (for [y (range 0 cols)]
          [cell-component x y])
        [:br]])])
