(ns spreadsheet.sheet
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]))

(def initial-rows 10)
(def initial-cols 10)

(def initial-sheet {:rows {}
                    :cols {}})

;; Handlers

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-sheet)))

(register-handler
  :add-cell
  (fn [db [_ x y cell]]
    ;; TODO: check if cell is correct before adding
    (-> db
        (assoc-in [:rows x y] cell)
        (assoc-in [:cols y x] cell))))

;; Subscriptions

(register-sub 
  :cell
  (fn [db [_ x y]]
    ;; TODO: also return cells that it uses for the formula
    (reaction (-> (:rows @db)
                  (get x)
                  (get y)))))

