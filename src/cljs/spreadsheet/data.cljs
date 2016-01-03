(ns spreadsheet.data
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]
            [spreadsheet.util :refer [charcode
                                      map-formula]]))


(def initial-sheet {:editing-cell nil
                    :clicked-cell nil
                    :temp-formula ""
                    :drag-cells nil
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

(defn update-formula
  "Returns a new local data store with
   the formula of a cell with row x and column
   y changed to a new formula"
  [db x y formula]
  (let [cell (-> (if-let [existing-cell (get-in db [:rows x y])]
                   existing-cell
                   initial-cell)
                 (assoc :formula formula)
                 (assoc :value (eval-formula formula)))]
    (-> db
        (assoc-in [:rows x y] cell)
        (assoc-in [:cols y x] cell))))

(defn cell-range [a b]
  (cond
    (> b a) (range (+ a 1) (+ b 1))
    (< b a) (range (+ b 1) (+ a 1))
    (= b a) nil))

(defn cell-increasing [a b]
  (cond
    (> b a) 1
    (< b a) -1
    (= b a) 0))

(defn x-increase-fn [x1 x2]
  #(vector %1 (+ %2 (cell-increasing x1 x2))))

(defn y-increase-fn [y1 y2]
  #(vector (str (+ (charcode %1) (cell-increasing y1 y2)) %2)))

(defn apply-increase-formula
  "Applies a function to a formula a certain number of times"
  [formula increase-fn num-times]
  (loop [formula formula
         times num-times]
    (if (= times 0)
      formula
      (recur (map-formula increase-fn formula) (- times 1)))))

(defn get-sel-range [[x1 y1] [x2 y2]]
  (cond
    (= x1 x2) (->> (cell-range y1 y2)
                   (map #(vector x1 %)))
    (= y1 y2) (->> (cell-range x1 x2)
                   (map #(vector % y1)))))

(defn get-sel-incr-fn [[x1 y1] [x2 y2]]
  (cond
    (= x1 x2) (y-increase-fn y1 y2)
    (= y1 y2) (x-increase-fn x1 x2)))

(defn get-sel-formula-range [p1 p2 formula]
  (let [increase-fn (get-sel-incr-fn p1 p2)
        sel-range (get-sel-range p1 p2)
        num-times-range (range 1 (+ (count sel-range) 1))
        handler-fn #(apply-increase-formula formula increase-fn %)]
    (map handler-fn num-times-range)))

(defn copy-cells
  "Returns a new local data store with
   the cells between the two cells populated with
   the copied formula"
  [db [x1 y1] [x2 y2] formula]
  (if (and (not (= x1 x2)) (not (= y1 y2)))
    (assoc db :drag-cells nil)
    (let [sel-range (get-sel-range [x1 y1] [x2 y2])
          formula-range (get-sel-formula-range [x1 y1] [x2 y2] formula)]
      (loop [cells sel-range
             formulas formula-range
             db db]
        (if (not (or (empty? cells) (empty? formulas)))
          (let [[x y] (first cells)
                formula (first formulas)]
            (recur (rest cells) (rest formulas) (update-formula db x y formula)))
          db)))))

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
          (assoc :drag-cells nil)
          (assoc :temp-formula "")))))

(register-handler
  :update-formula
  (fn [db [_ x y formula]]
    (update-formula db x y formula)))

(register-handler
  :clicked-cell
  (fn [db [_ x y]]
    (if-let [[ex ey] (:editing-cell db)]
      (if (not (and (= ex x) (= ey y)))
        (assoc db :clicked-cell [x y])
        db)
      db)))

(register-handler
  :right-click-cell
  (fn [db [_ x y]]
    (if (nil? (:drag-cells db))
      (let [formula (if-let [existing-cell (get-in db [:rows x y])]
                      (:formula existing-cell)
                      "")]
        (assoc db :drag-cells [formula x y]))
      (let [[formula x1 y1] (:drag-cells db)
            [x2 y2] [x y]]
        (assoc (copy-cells db [x1 y1] [x2 y2] formula) :drag-cells nil)))))

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
