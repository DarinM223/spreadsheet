(ns spreadsheet.cell
  (:require [re-frame.core :refer [subscribe
                                   dispatch]]
            [spreadsheet.data :refer [cell-str]]

(def keycodes {:enter 13})

(defn handle-keydown
  "Handler for keypresses inside cell textfield
   that catches enter presses"
  [e]
  (if (= (.-keyCode e) (:enter keycodes))
    (dispatch [:cell-lose-focus])))

;; Views

(defn cell-field
  "Generates the HTML layout for a cell"
  [x y cell]
  [:span
   [:input {:type "text"
            :value (if (:editing cell)
                     (if-let [[cx cy] (:clicked-cell cell)]
                       (str (:temp-formula cell) (cell-str cx cy))
                       (:temp-formula cell))
                     (:value cell))
            :on-change #(dispatch [:change-temp-formula (-> % .-target .-value)])
            :readOnly (nil? (:editing cell))
            :on-click (fn [e]
                        (dispatch [:clicked-cell x y])
                        (.stopPropagation e))
            :on-keyDown handle-keydown
            :on-doubleClick #(dispatch [:double-click-cell x y])}]])

(defn cell-component
  "A React component for a Google Sheets style cell"
  [x y]
  (fn []
    (if-let [cell (subscribe [:cell x y])]
      (cell-field x y @cell)
      (cell-field x y {:formula "" :value ""}))))
