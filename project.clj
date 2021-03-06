(defproject translation-core-single "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.0"]
                 [midje "1.9.9"]
                 [http-kit "2.3.0"]
                 [ring "1.8.0"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "1.0.0"]
                 ]
  :main ^:skip-aot translation-core-single.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
