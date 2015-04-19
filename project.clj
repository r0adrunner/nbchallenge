(defproject nbgraph "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.novemberain/monger "1.4.2"]
                 [compojure "1.3.1"]
                 [cheshire "5.4.0"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.1.2"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler nbgraph.controller.handler/app
         :init nbgraph.controller.handler/init}
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})

