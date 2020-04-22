(ns translation-core-single.core
  (:require [org.httpkit.server :refer [run-server]]
            [translation-core-single.routes :refer [app]]
            )
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-server (app) {:port 3004}))
