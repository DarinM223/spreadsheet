(ns spreadsheet.util)

(def char-code-0 48)
(def char-code-9 57)

(defn charcode
  "Returns the integer code of a character represented
   as a string with one element"
  [ch]
  #?(:clj (int (first ch))
     :cljs (.charCodeAt ch 0)))

(defn is-digit
  "Returns true if character is a digit, otherwise returns false"
  [ch]
  (if (= (count ch) 0)
    false
    (let [code (charcode ch)]
      (and (>= code char-code-0) (<= code char-code-9)))))

(defn first-str
  "Returns the first character of a string
  or an empty string if there are no characters
  Example: 'Hello' -> 'H', '' -> ''"
  [str]
  (if (<= (count str) 0)
    ""
    (subs str 0 1)))

(defn rest-str
  "Returns the string with the first character removed
  or an empty string if there are no characters
  Example: 'Hello' -> 'ello', '' -> ''"
  [str]
  (if (<= (count str) 0)
    ""
    (subs str 1 (count str))))

(defn str-to-int
  "Converts a string to an integer representation"
  [str]
  #?(:clj (Integer/parseInt str)
     :cljs (js/parseInt str 10)))

(defn split-num-from-str
  "Takes a number from the beginning of the string
   and returns the number and the rest of the string
   Example: '100 AD' -> [100 ' AD']"
   [s]
   (loop [num-str ""
          s s]
     (let [ch (first-str s)]
       (if (is-digit ch)
         (recur (str num-str ch) (rest-str s))
         [#?(:clj (Integer/parseInt num-str)
             :cljs (js/parseInt num-str 10)) s]))))
