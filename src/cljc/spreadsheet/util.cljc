(ns spreadsheet.util)

(def char-code-0 48)
(def char-code-9 57)
(def char-code-A 65)
(def char-code-Z 90)

(defn charcode
  "Returns the integer code of a character represented
   as a string with one element"
  [ch]
  #?(:clj (int (first ch))
     :cljs (.charCodeAt ch 0)))

(defn is-digit
  "Returns true if character is a digit, otherwise returns false"
  [ch]
  (if (empty? ch)
    false
    (let [code (charcode ch)]
      (and (>= code char-code-0) (<= code char-code-9)))))

(defn is-capital
  "Returns true if character is a capital letter, otherwise returns false"
  [ch]
  (if (empty? ch)
    false
    (let [code (charcode ch)]
      (and (>= code char-code-A) (<= code char-code-Z)))))

(defn first-str
  "Returns the first character of a string
   or an empty string if there are no characters
   Example: 'Hello' -> 'H', '' -> ''"
  [str]
  (if (empty? str)
    ""
    (subs str 0 1)))

(defn rest-str
  "Returns the string with the first character removed
   or an empty string if there are no characters
   Example: 'Hello' -> 'ello', '' -> ''"
  [str]
  (if (empty? str)
    ""
    (subs str 1 (count str))))

(defn str-to-int
  "Converts a string to an integer representation"
  [str]
  (if (empty? str)
    nil
    #?(:clj (Integer/parseInt str)
       :cljs (js/parseInt str 10))))

(defn str-to-int
  "Converts a number string to a number, or nil if
   the string is empty
   Example: '100' -> 100, '' -> nil"
  [str]
  (if (empty? str)
    nil
    #?(:clj (Integer/parseInt str)
       :cljs (js/parseInt str))))

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
        [(str-to-int num-str) s]))))

(defn increment-formula
  "Increments all of the cells in a spreadsheet formula
   Example: 'A1 + B1' -> 'A2 + B2'"
  [formula]
  (loop [built-string ""
         rest-string formula]
    (if (not (empty? rest-string))
      (let [ch (first-str rest-string)
            rst (rest-str rest-string)]
        (if (is-capital ch)
          (let [[num new-rst] (split-num-from-str rst)]
            (if (not (nil? num))
              (recur (str built-string ch (+ num 1)) new-rst)
              (recur (str built-string ch) new-rst)))
          (recur (str built-string ch) rst)))
      built-string)))
