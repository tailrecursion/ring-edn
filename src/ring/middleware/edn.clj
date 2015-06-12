(ns ring.middleware.edn
  (:require clojure.edn))

(defn- edn-request?
  [req]
  (if-let [^String type (get-in req [:headers "content-type"] "")]
    (not (empty? (re-find #"^application/(vnd.+)?edn" type)))))

(defprotocol EdnRead
  "Specifies that the object can be read and transformed to edn"
  (-read-edn [this] [this opts]
    "Transforms the serialized object into edn.
     May take an opts map to pass to clojure.edn/read-string"))

(extend-type String
  EdnRead
  (-read-edn
    ([s] (-read-edn s {}))
    ([s opts]
     (clojure.edn/read-string opts s))))

(extend-type java.io.InputStream
  EdnRead
  (-read-edn
    ([is] (-read-edn is {}))
    ([is opts]
     (clojure.edn/read
       (merge {:eof nil} opts)
       (java.io.PushbackReader.
         (java.io.InputStreamReader. is "UTF-8"))))))

(defn wrap-edn-params
  "If the request has the edn content-type, it will attempt to read
  the body as edn and then assoc it to the request under :edn-params
  and merged to :params.

  It may take an opts map to pass to clojure.edn/read-string"
  ([handler] (wrap-edn-params handler {}))
  ([handler opts]
   (fn [req]
     (if-let [body (and (edn-request? req) (:body req))]
       (let [edn-params (binding [*read-eval* false] (-read-edn body opts))
             req* (assoc req
                    :edn-params edn-params
                    :params (merge (:params req) edn-params))]
         (handler req*))
       (handler req)))))
