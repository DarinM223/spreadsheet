(ns spreadsheet.data
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]))

(def initial-rows 10)
(def initial-cols 10)

(def initial-sheet {:editing-cell nil
                    :rows {}
                    :cols {}})

(def initial-cell {:formula ""
                   :value ""})

(defn eval-formula 
  "Evaluates the formula and returns the value
   TODO: implement this"
  [formula] formula)

;; Handlers

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-sheet)))

(register-handler
  :update-formula
  (fn [db [_ x y formula]]
    ;; TODO: check if cell is correct before adding
    (let [cell (if-let [existing-cell (get-in db [:rows x y])]
                 existing-cell
                 initial-cell)]
      (let [updated-cell (-> cell
                             (assoc :formula formula)
                             (assoc :value (eval-formula formula)))]
        (-> db
            (assoc-in [:rows x y] updated-cell)
            (assoc-in [:cols y x] updated-cell))))))


(register-handler
  :double-click-cell
  (fn [db [_ x y]]
    (assoc db :editing-cell [x y])))

(register-handler
  :cell-lose-focus
  (fn [db [_ x y]]
    (assoc db :editing-cell nil)))

;; Subscriptions

(register-sub
  :editing-cell
  (fn [db _]
    (reaction (:editing-cell @db))))

(register-sub 
  :cell
  (fn [db [_ x y]]
    ;; TODO: also return cells that it uses for the formula
    (let [cell (if-let [existing-cell (get-in @db [:rows x y])]
                 existing-cell
                 initial-cell)]
      (if-let [[ex ey] (:editing-cell @db)]
        (if (and (= x ex) (= y ey))
          (reaction (assoc cell :editing true))
          (reaction cell))
        (reaction cell)))))

