(ns spreadsheet.cell
  (:require [re-frame.core :refer [subscribe
                                   dispatch]]))

;; Views

(defn cell-field [x y cell]
  [:div
   [:input {:type "text" :value (:value cell)
            :on-change #(dispatch [:update-formula x y (-> % .-target .-value)])
            :readOnly (nil? (:editing cell)) 
            :on-click #(.stopPropagation %)
            :on-doubleClick #(dispatch [:double-click-cell x y])}]])

(defn cell-component [x y]
  (fn []
    (if-let [cell (subscribe [:cell x y])]
      (cell-field x y @cell)
      (cell-field x y {:formula "" :value ""}))))

