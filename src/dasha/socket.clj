(ns dasha.socket
  (:require
    [clojure.core.async :refer [chan go go-loop <! >! timeout close!]]
    [clojure.core.async :as cca]

    [dasha.widgets.weather :as dww]
    [dasha.widgets.clojure :as dwc]

    [org.httpkit.server :as ohs]
    [org.httpkit.timer :as oht]
    [cheshire.core :as cc]

    [dasha.widgets :as dw]))

(def clients (atom #{}))

(defn add-client [c] (swap! clients conj c))
(defn rm-client  [c]
  (swap! clients
         (fn [cs]
           (remove #(= c %) cs))))

(defn send-to-all [msg]
  (doseq [c @clients] (try (ohs/send! c (cc/generate-string msg)))))

(defn async-handler [req]
  (ohs/with-channel req ws
    (if (ohs/websocket? ws)
      (println "WebSocket channel")
      (println "HTTP channel"))

    (add-client ws)

    (ohs/on-close ws
                  (fn  [status]
                    (println "Channel " ws " status " status)
                    (rm-client ws)))

    (ohs/on-receive ws #(println "Data from socket " %))))


(defn start []
  (let [out (chan)
        ctrl (chan)]
    (go-loop [] (send-to-all (<! out)) (recur))

    (dww/widget ctrl out {:t 3000 :qs ["Saint Petersburg" "Moscow" "Kiev"]})
    (dwc/widget ctrl out {:t 10000})

    (fn [] (go (>! ctrl :stop) (close! ctrl)))))

(def stop (start))
;(stop)