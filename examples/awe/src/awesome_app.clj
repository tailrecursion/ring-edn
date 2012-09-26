(ns awesome-app
  (:use ring.adapter.jetty)
  (:require [my-awesome-service :as awe]))

(defn -main
  [& args]
  (run-jetty #'awe/app {:port 8080}))
