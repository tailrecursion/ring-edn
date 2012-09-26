(ns my-awesome-service
  (:use compojure.core)
  (:use ring.middleware.edn))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (print-str data)})
  
(defroutes handler
  (GET "/" []
       (generate-response {:hello :cleveland}))

  (PUT "/" [name]
       (generate-response {:hello name})))

(def app
  (-> handler
      wrap-edn-params))
