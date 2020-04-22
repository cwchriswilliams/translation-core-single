(ns translation-core-single.page-handlers.script-test
  (:require [midje.sweet :refer :all]
            [translation-core-single.page-handlers.script :refer :all]))

(future-facts "create-page-template-header"
       (fact "Creates as expected"
             (create-page-template-header) => [:head [:title "Translation Core"]]
             )
       )


(facts "create-page-template-body"
       (fact "Creates as expected"
             (create-page-template-body [:h1 "Body Content"]) => [:body [:h1 "Body Content"]]
             )
       )

(facts "create-page-template"
       (fact "Creates template from combined head and body"
             (create-page-template ..content..) => "<!DOCTYPE html>\n<html><h1>TestH1</h1><h2>TestH2</h2></html>"
             (provided
               (create-page-template-header) => [:h1 "TestH1"]
               (create-page-template-body anything) => [:h2 "TestH2"])
             )
       )

(fact "load-script-content"
      (load-script-content) => "script-content"
      (provided
        (slurp anything) => "script-content"
        )
      )

(fact "load-script-content->map"
      (load-script-content->map) => {:script-lines [{:speaker "1" :value "V1"}{:speaker "2" :value "V2"}]}
      (provided
        (load-script-content) => "{\"script-lines\":[{\"speaker\":\"1\",\"value\":\"V1\"}{\"speaker\":\"2\",\"value\":\"V2\"}]}"
        )
      )

(fact "build-script-line-display"
      (build-script-line-display ..character-list.. {:speaker ..speakerId.. :translations ..translations..}) =>
      [:div {:class "card" } [:h3 {:class "card-header"} ..speakerName..] [:div {:class "card-body"} ..translationDisplay..]]
      (provided
        (get-speaker-name-by-id ..character-list.. ..speakerId..) => ..speakerName..
        (get-translations-displays ..translations..) => [..translationDisplay..]
        )
      (build-script-line-display ..character-list.. {:speaker ..speakerId.. :translations [..translation-1.. ..translation-2..]}) =>
      [:div {:class "card" } [:h3 {:class "card-header"} ..speakerName..] [:div {:class "card-body"} ..translation-Display-1.. ..translation-display-2..]]
      (provided
        (get-speaker-name-by-id ..character-list.. ..speakerId..) => ..speakerName..
        (get-translations-displays [..translation-1.. ..translation-2..]) => [..translation-Display-1.. ..translation-display-2..]
        )
      )

(fact "build-script-display-from-lines"
      (build-script-lines-display [] []) => [:i "Empty"]
      (build-script-lines-display [] [{:speaker "1"}])
      )

(fact "get-speaker-name-by-id"
      (get-speaker-name-by-id ..character-list.. ..id.. ) => ..speaker-name..
      (provided
        (get-speaker-by-id ..character-list.. ..id..) => {:aliases [[{:test-alias 1}] [{:test-alias 2}] ]}
        (get-primary-value-from-translations anything) => ..speaker-name..
      ))

(fact "get-speaker-by-id"
      (get-speaker-by-id [{:id ..id-1..} {:id ..search-id..}] ..search-id..) => {:id ..search-id..}
      )

(fact "get-primary-value-from-translations"
      (get-primary-value-from-translations [{:value ..translation-value.. :is-primary true}]) => ..translation-value..
      (get-primary-value-from-translations [{:is-primary false} {:value ..translation-value.. :is-primary true}]) => ..translation-value..
      )

(fact "get-translations-display"
      (get-translations-displays [..translation-1.. ..translation-2..]) => (every-checker vector? [..translation-display-1.. ..translation-display-2..])
      (provided
        (get-translation-display ..translation-1..) => ..translation-display-1..
        (get-translation-display ..translation-2..) => ..translation-display-2..
        )
      )

(fact "get-translation-display"
      (get-translation-display {:value "Value1"}) => [:div "Value1"]
      )

(fact "load-character-list-content"
      (load-character-list-content) => "character-list-content"
      (provided
        (slurp anything) => "character-list-content"
        )
      )

(fact "load-character-list-content->map"
      (load-character-list-content->map) => {:character-list [{:id 1 :aliases [{:value "TestValue"}]}]}
      (provided
        (load-character-list-content) => "{\"character-list\":[{\"id\":1,\"aliases\": [{\"value\": \"TestValue\"}]}]}"
        )
      )

(future-fact
  (show-script) => "SomeOtherText")

(future-fact "build-add-line-form"
      (build-add-line-form []) => [:form ]
      )

(facts "Within integration"
       (let [character-list [{:id 1 :aliases [{:value "Bob" :is-primary true}]}]
             script-line {:speaker 1 :translations [{:value "Hello"} {:value "Hello2"}]}
             ]
       (build-script-line-display character-list script-line)) => [:div {:class "card"} [:h3 {:class "card-header" } "Bob"] [:div {:class "card-body"} [:div "Hello"] [:div "Hello2"]]]

       )

