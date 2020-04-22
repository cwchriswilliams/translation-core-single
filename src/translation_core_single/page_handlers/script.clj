(ns translation-core-single.page-handlers.script
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.form :refer :all]
            [clojure.data.json :as json]
            [clojure.string :as s])
  (:import (java.util UUID)))

(def script-root "../resources/translation-core-single/scripts/")

(defn build-script-path
  [title script-name]
  (str script-root title "/" script-name ".json"))


(defn build-character-list-path
  [title]
  (str script-root title "/character-list.json"))

(defn build-phrasebook-path [title phrasebook-name]
  (str script-root title "/" phrasebook-name "-phrasebook.json"))


(def script-set (slurp (str script-root "script-set.json")))

(defn script-set->map
  []
  (json/read-str script-set :key-fn keyword)
  )


(defn script-details->map
  [directory]
  (json/read-str (slurp (str script-root directory "/details.json")) :key-fn keyword)
  )

(defn create-page-template-header
  [page-title]
  [:head
   [:title page-title]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css")
   (include-css "https://cdnjs.cloudflare.com/ajax/libs/flag-icon-css/3.4.6/css/flag-icon.min.css")
   (include-css "https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css")
   ]
  )

(defn build-script-route
  [title script-name]
  (str "/script/" title "/" script-name))


(defn build-phrasebook-route
  [title phrasebook-name]
  (str "/phrasebook/" title "/" phrasebook-name))

(defn build-script-details-menu
  [script-item]
  [:div {:class "dropdown-menu"}
  (for [script-details (script-details->map (:directory script-item))]
          (if (= (:type script-details) "phrasebook")
            [:a {:class "dropdown-item" :href (build-phrasebook-route (:directory script-item) (:name script-details))} (:title script-details)]
            [:a {:class "dropdown-item" :href (build-script-route (:directory script-item) (:name script-details))} (:title script-details)]
            ))
   ]
 )

(defn create-nav-bar
  [title]
  [:nav {:class "navbar navbar-light bg-light navbar-expand-lg"}
   [:button {:class "navbar-toggler" :data-toggle "collapse" :data-target "#navbar-content"}
    [:span {:class "navbar-toggler-icon"}]]
   [:div {:class "collapse navbar-collapse" :id "navbar-content"}
    [:ul {:class "navbar-nav mr-auto"}
    [:ul {:class "navbar-nav mr-auto"}
     (for [script-item (script-set->map)]
       [:li {:class "dropdown nav-item"}
        [:a {:class "nav-link dropdown-toggle" :href "#" :data-toggle "dropdown"} (:title script-item)]
        (build-script-details-menu script-item)
        ]
       )
      ]]
      (form-to {:class "form-inline my-2"} [:post (str "/search/" title)]
               (text-field {:class "form-control mr-1" :type "search" :placeholder "Search"} "search-field")
               (submit-button {:class "btn btn-outline-success"} "Search"))
    ]]
  )

(defn add-js-scripts
  []
  [:div
   (include-js "https://code.jquery.com/jquery-3.4.1.slim.min.js")
   (include-js "https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js")
   (include-js "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js")
   ])

(defn create-page-template-body
  [body-content]
  [:body
   [:div body-content]
   ]
  )

(defn create-page-template
  [title page-title content]
  (html5
    (create-page-template-header page-title)
    (let [nav-bar (create-nav-bar title)
          body-content [:div nav-bar content]]
      (create-page-template-body body-content))
    (add-js-scripts)
    )
  )

(defn load-script-content
  [title script-name]
  (slurp (build-script-path title script-name)
  ))

(defn load-character-list-content
  [title]
  (slurp (build-character-list-path title))
  )


(defn load-script-content->map
  [title script-name]
  (json/read-str (load-script-content title script-name) :key-fn keyword)
  )

(defn load-character-list-content->map
  [title]
  (json/read-str (load-character-list-content title) :key-fn keyword)
  )

(defn translation-lines
  [value]
  (for [line (s/split-lines value)]
    [:div line])
  )

(defn get-translation-display
  [translations]
  (let [language (:language translations)
        value-lines (translation-lines (:value translations))]
    (cond
      (or (= "phrase" language) (= "japanese" language)) [:div {:class "row"} [:div {:class "col-1"} [:span {:class "flag-icon flag-icon-jp float-right"}]] [:div {:class "mb-1"} value-lines]]
      (= "english" language) [:div {:class "row"} [:div {:class "col-1"} [:span {:class "flag-icon flag-icon-gb float-right"}]] [:div {:class "mb-1"} value-lines]]
      (= "hiragana" language) [:div {:class "row"}  [:span {:class "text-muted col-1 text-right"} "平仮名"] [:div {:class "mb-1"} value-lines]]
      (= "katakana" language) [:div {:class "row"}  [:span {:class "text-muted col-1 text-right"} "片仮名"] [:div {:class "mb-1"} value-lines]]
      :else [:div {:class "mb-1"} value-lines]))
    )

(defn get-translations-displays
  [translations]
  (mapv get-translation-display translations)
  )

(defn get-primary-value-from-translations
  [translations]
  (:value (first (filter :is-primary translations))
  ))

(defn get-speaker-by-id
  [character-list character-id]
  (first (filter #(= character-id (:id %)) character-list)))

(defn get-primary-name-from-character
  [character]
  (-> character
      (:aliases)
      (flatten)
      (get-primary-value-from-translations)))

(defn get-speaker-name-by-id
  [character-list character-id]
    (-> (get-speaker-by-id character-list character-id)
        (get-primary-name-from-character)))

(defn build-translation-card-display
  [translations]
  (vec (concat [:div {:class "card-body"}] (get-translations-displays translations))))

(defn build-script-line-display
  [character-list script-line]
  (let[translation-display-elements (build-translation-card-display (:translations script-line))
       retValue   [:div {:class "card" } [:div {:class "card-header row"}
                                          [:h3 {:class "col" } (get-speaker-name-by-id character-list (:speaker script-line))]
                                          [:div {:class "col-2"}
                                           [:button {:class "btn btn-primary m-1 float-right"} [:i {:class "fa fa-edit"}]]
                                           [:button {:class "btn btn-danger m-1 float-right"} [:i {:class "fa fa-trash"}]]]
                                          ]
                   translation-display-elements]]
    retValue)
)


(defn build-script-lines-display
  [character-list script-lines]
  (if (empty? script-lines)
    [:i "Empty"]
    (let [result (map #(build-script-line-display character-list %) script-lines)]
      result)
    )
  )

(defn get-all-speaker-primary-names
  [character-list]
  (map get-primary-name-from-character character-list))


(defn validation-for
  [model field]
  (let [all-errors (:errors model)
        field-errors (filter #(= field (:field %)) all-errors)]
  (for [error field-errors] [:div {:class "text-danger"} (:error error)])
  ))

(defn validation-summary
  [model]
  (validation-for model ""))

(defn build-add-line-form
  [title script-name character-names model]
  (form-to {:class "m-3"} [:post (str "/script/" title "/" script-name)]
           [:div {:class "form-group"}
            [:div {:class "form-group"}
             (drop-down {:class "form-control"} "speaker" character-names (:speaker model))
             ]
            [:div {:class "form-group"}
             (label {:class "form-control"} "japanese" "Japanese")
             (text-area {:class "form-control" :rows "3"} "japanese" (:japanese model))
             (validation-for model "japanese")
             ]
            [:div {:class "form-group"}
             (label {:class "form-control"} "english" "English")
             (text-area {:class "form-control" :rows "3"} "english" (:english model))
             (validation-for model "english")
             ]
            (validation-summary model)
            [:div {:class "form-group"}
             (submit-button {:class "btn btn-primary"} "Add")]
            ]
           ))


(defn build-add-phrase-form
  [title phrasebook-name model]
  (form-to {:class "m-3"} [:post (str "/phrasebook/" title "/" phrasebook-name)]
           [:div {:class "form-group"}
            [:div {:class "form-group"}
             (label {:class "form-control"} "japanese" "Japanese")
             (text-field {:class "form-control"} "japanese" (:japanese model))
             ]
            [:div {:class "form-group"}
             (label {:class "form-control"} "hiragana" "平仮名")
             (text-field {:class "form-control"} "hiragana" (:hiragana model))
             ]
            [:div {:class "form-group"}
             (label {:class "form-control"} "english" "English")
             (text-field {:class "form-control"} "english" (:english model))
             ]
            (validation-for model "english")
            (validation-summary model)
            [:div {:class "form-group my-4"}
             (submit-button {:class "btn btn-primary"} "Add")]
            ]
           ))

(defn get-character-list-position
  [ordered-list-of-speakers character]
  (let [index-in (.indexOf ordered-list-of-speakers (:id character))]
    (if (= -1 index-in) 9999 index-in)))

(defn character-list->sorted-by-recently-used
  [character-list script-lines]
  (let [ordered-list-of-speakers (take 10 (distinct (map :speaker script-lines)))]
    (sort-by #(get-character-list-position ordered-list-of-speakers %) character-list)
    )
  )

(defn show-script
  ([title script-name]
   (show-script title script-name {}))
  ([title script-name model]
  (let [script-set-details (script-set->map)
        script-details (script-details->map title)
        character-list (load-character-list-content->map title)
        script-lines (load-script-content->map title script-name)
        reversed-script-lines (reverse script-lines)
        ordered-character-list (character-list->sorted-by-recently-used character-list reversed-script-lines)
        script-lines-display (build-script-lines-display ordered-character-list reversed-script-lines)
        character-primary-names (get-all-speaker-primary-names ordered-character-list)
        page-contents [:div (build-add-line-form title script-name character-primary-names model) script-lines-display]]
    (create-page-template title (str title " - " script-name) page-contents))
  ))

(defn get-speaker-id-from-name
  [character-list speaker-name]
  (:id (first (filter #(= (get-primary-name-from-character %) speaker-name) character-list))))

(defn build-translation-entry
  [speaker-id japanese english]
  {
   :id           (str (UUID/randomUUID))
   :speaker speaker-id :translations [
                                      {:language "japanese" :value japanese}
                                      {:language "english" :value english}
                                      ]})


(defn load-phrasebook-content [phrasebook-path]
  (slurp phrasebook-path))

(defn load-phrasebook-content->map
  [phrasebook-path]
  (json/read-str (load-phrasebook-content phrasebook-path) :key-fn keyword)
  )

(defn build-translation-card-entry
  [translation]
  [:div {:class "card"} (build-translation-card-display (:translations translation))])

(defn show-phrasebook
  ([title phrasebook-name] (show-phrasebook title phrasebook-name {}))
  (
  [title phrasebook-name model]
  (let [
        phrasebook-path (build-phrasebook-path title phrasebook-name)
        phrasebook-contents (load-phrasebook-content->map phrasebook-path)
        reversed-contents (reverse phrasebook-contents)
        all-translations (mapv build-translation-card-entry reversed-contents)
        translation-page-contents (vec (concat [:div] all-translations))
        page-contents [:div (build-add-phrase-form title phrasebook-name model) translation-page-contents]]
  (create-page-template title (str title " - " phrasebook-name " - Phrasebook") page-contents))
  ))

(defn build-phrasebook-entry
  [japanese hiragana english]
  (let [translations [
                      {:language "phrase" :value japanese}
                      {:language "hiragana" :value hiragana}
                      {:language "english" :value english}
                      ]
        no-nils (remove #(nil? (:value %)) translations)]
    {
     :id           (str (UUID/randomUUID))
     :translations no-nils
     }))

(defn validate-phrasebook-entry
  [model]
  (assoc model :errors
               (filter (comp not nil?) [
                  (when (empty? (:english model)) {:field "english" :error "English must be provided"})
                  (when (and (empty? (:japanese model)) (empty? (:hiragana model))) {:field "" :error "Either Japanese or Hiragana must be provided"})
                  ]))
)

(defn save-phrasebook-entry
  [title phrasebook-name japanese hiragana english]
  (let [validated-model (validate-phrasebook-entry {:japanese japanese :hiragana hiragana :english english})]
    (if (not (empty? (:errors validated-model)))
    (show-phrasebook title phrasebook-name validated-model)
    (let [phrasebook-path (build-phrasebook-path title phrasebook-name)
          original-ohrasebook-map (load-phrasebook-content->map phrasebook-path)
          new-entry (build-phrasebook-entry japanese hiragana english)
          phrasebook-with-new-phrase (conj original-ohrasebook-map new-entry)
          new-phrasebook-json (json/write-str phrasebook-with-new-phrase :escape-unicode false)
          ]
      (spit phrasebook-path new-phrasebook-json)
      (show-phrasebook title phrasebook-name)))
  ))

(defn validate-script-entry
  [model]
  (assoc model :errors
               (filter (comp not nil?) [
                                        (when (empty? (:english model)) {:field "english" :error "English must be provided"})
                                        (when (empty? (:japanese model)) {:field "japanese" :error "Japanese must be provided"})
                                        ]))
  )

(defn save-script-entry
  [title script-name speaker japanese english]
  (let [validated-model (validate-script-entry {:japanese japanese :english english})]
    (if (not (empty? (:errors validated-model)))
      (show-script title script-name validated-model)
      (let [original-script-map (load-script-content->map title script-name)
            speaker-id (get-speaker-id-from-name (load-character-list-content->map title) speaker)
            new-entry (build-translation-entry speaker-id japanese english)
            script-lines-with-new-line (conj original-script-map new-entry)
            new-script-map-json (json/write-str script-lines-with-new-line :escape-unicode false)
            ]
        (spit (build-script-path title script-name) new-script-map-json)
        (show-script title script-name)
        )
      )
    )
  )

(defn to-translation
  [input]
  (:translations input))

(defn load-details-from-entry
  [title entry]
  (cond
    (= (:type entry) "phrasebook") (map to-translation (load-phrasebook-content->map (build-phrasebook-path title (:name entry))))
    (= (:type entry) "script") (map to-translation (load-script-content->map title (:name entry)))
    )
  )

(defn load-all-translations->map
  [title]
  (let [details (script-details->map title)
        sorted (sort-by #(:type %) details)]
    (for [details-entry sorted]
      (load-details-from-entry title details-entry)))
  )

(defn any-matches-in-translation
  [regex translation]
  (let [result (some #(re-matches regex (:value %)) translation)]
    result))

(defn find-in-list
  [entries search-field]
  (let [pattern (re-pattern (str "(?i).*" search-field ".*"))]
    (filter #(any-matches-in-translation pattern %) entries)
    )
)


(defn build-search-results-list
  [title search-field]
  [:div (map build-translation-card-display (find-in-list (apply concat (load-all-translations->map title)) search-field))])

(defn show-search-results
  [title search-field]
  (let [page-contents [:div (build-search-results-list title search-field)]]
    (create-page-template title (str title " - Search Results - " search-field) page-contents)
    )
  )