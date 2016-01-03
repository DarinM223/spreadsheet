(ns spreadsheet.data
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]))

(def initial-sheet {:editing-cell nil
                    :clicked-cell nil
                    :temp-formula ""
                    :rows {}
                    :cols {}})

(def initial-cell {:formula ""
                   :value ""})

(defn eval-formula
  "Evaluates the formula and returns the value
   TODO: implement this"
  [formula] formula)

(defn validate-formula
  "Checks if a formula is valid
   TODO: implement this"
  [formula]
  true)

(defn cell-str
  "Returns the string description
   of the cell
   Example: cell at row 5 and column 0
   will return A5"
  [x y]
  (str (char (+ x (.charCodeAt "A" 0))) y))

(defn persist-formula
  "Returns a new local data store with
   the temporary formula persisted to
   the editing cell"
  [db]
  (if-let [[x y] (:editing-cell db)]
    (let [cell (if-let [existing-cell (get-in db [:rows x y])]
                 existing-cell
                 initial-cell)]
      (if-let [formula (if-let [[cx cy] (:clicked-cell db)]
                         (str (:temp-formula db) (cell-str cx cy))
                         (:temp-formula db))]
        (if (validate-formula formula)
          (let [updated-cell (-> cell
                                 (assoc :formula formula)
                                 (assoc :value (eval-formula formula)))]
              (-> db
                  (assoc-in [:rows x y] updated-cell)
                  (assoc-in [:cols y x] updated-cell)))
          db)
        db))
    db))

;; Handlers

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-sheet)))

(register-handler
  :change-temp-formula
  (fn [db [_ formula]]
    (-> db
        (assoc :temp-formula formula)
        (assoc :clicked-cell nil))))

(register-handler
  :double-click-cell
  (fn [db [_ x y]]
    (-> db
        (assoc :editing-cell [x y])
        (assoc :clicked-cell nil)
        (assoc :temp-formula (if-let [{formula :formula} (get-in db [:rows x y])]
                               formula
                               "")))))

(register-handler
  :cell-lose-focus
  (fn [db _]
    ; persist temporary formula and clear temporary fields
    (let [updated-db (persist-formula db)]
      (-> updated-db
          (assoc :editing-cell nil)
          (assoc :clicked-cell nil)
          (assoc :temp-formula "")))))

(register-handler
  :clicked-cell
  (fn [db [_ x y]]
    (if-let [[ex ey] (:editing-cell db)]
      (if (not (and (= ex x) (= ey y)))
        (assoc db :clicked-cell [x y])
        db)
      db)))

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
        (if (and (= x ex) (= y ey)) ; check if the subscribed cell is the current editing cell
          (reaction (-> cell
                        (assoc :editing true)
                        (assoc :temp-formula (:temp-formula @db))
                        (assoc :clicked-cell (:clicked-cell @db))))
          (reaction cell))
        (reaction cell)))))
