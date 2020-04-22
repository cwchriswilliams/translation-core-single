(ns translation-core-single.routes
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer :all]
            [translation-core-single.page-handlers.script :refer [show-script save-script-entry show-phrasebook save-phrasebook-entry show-search-results]]
            ))

(defroutes app-routes
           (GET "/" [] (redirect "/script/pwaa/first-turnabout"))
           (GET "/script/:title/:script-name" [title script-name] (show-script title script-name))
           (POST "/script/:title/:script-name" [title script-name speaker japanese english] (save-script-entry title script-name speaker japanese english))
           (GET "/phrasebook/:title/:phrasebook-name" [title phrasebook-name] (show-phrasebook title phrasebook-name))
           (POST "/phrasebook/:title/:phrasebook-name" [title phrasebook-name japanese hiragana english] (save-phrasebook-entry title phrasebook-name japanese hiragana english))
           (POST "/search/:title" [title search-field] (show-search-results title search-field))
           )

(defn app []
  (-> app-routes wrap-reload wrap-params))
