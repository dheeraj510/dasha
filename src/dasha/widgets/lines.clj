(ns dasha.widgets.lines
  (:require [org.httpkit.client :as http]
            [clojure.string :as cs]
            [hiccup.core :as hc]))

(defn widget [out cfg]
  (let [url (:url cfg)]
    (http/get (:url cfg) {}
              (fn [{b :body}]
                (let [ls (cs/split b #"\n")
                      ln (or (rand-nth ls) ":(")]
                  (out {:color (or (:color cfg) "#7e3878")
                        :summary (hc/html [:h1 (:title cfg)] [:p ln])
                        :full    (hc/html [:h1 (:title cfg)] [:hr] [:big ln])}))))
    cfg))
