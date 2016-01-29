(ns spreadsheet.redis
  (:require [taoensso.carmine :as car]
            [crypto.random :as random]))

;; Redis connection options
(def conn {:pool {} :spec {}})

(defmacro wcar* [& body] `(car/wcar conn ~@body))

(defn generate-random-hash [size]
  (loop []
    (let [id (random/url-part size)
          is-unique (= (wcar* (car/exists id)) 0)]
      (if is-unique
        id
        (recur)))))

(defn set-cell
  [cell]
  (let [hash (generate-random-hash 30)]
    {:retcode (wcar* (car/set hash cell))
     :hash hash}))

(defn get-cell [id]
  (if (= (wcar* (car/exists id)) 1)
    {:cell (wcar* (car/get id))}
    {:err "Cell does not exist"}))

(defn remove-cell [id]
  (if (= (wcar* (car/exists id)) 1)
    (if (= (wcar* (car/del id)) 1)
      {:success true}
      {:success false})
    {:err "Cell does not exist"}))
