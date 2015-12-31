(ns spreadsheet.cell
  (:require [re-frame.core :refer [subscribe
                                   dispatch]]))

;; Views

(defn cell [x y]
  (fn []
    (if-let [cell (subscribe [:cell x y])]
      (do
        [:div
         [:input {:type "text" :value (:value @cell)
                  :on-change #(dispatch [:update-formula x y (-> % .-target .-value)])}]])
      (do
        [:div
         [:input {:type "text" :value ""
                  :on-change #(dispatch [:update-formula x y (-> % .-target .-value)])}]]))))

