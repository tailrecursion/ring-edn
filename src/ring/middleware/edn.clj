(ns ring.middleware.edn
  (:require clojure.edn))

(defn- edn-request?
  [req]
  (if-let [^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?edn" type)))))

(defprotocol EdnRead
  (-read-edn [this]))

(extend-type String
  EdnRead
  (-read-edn [s]
    (clojure.edn/read-string s)))

(extend-type java.io.InputStream
  EdnRead
  (-read-edn [is]
    (clojure.edn/read
     {:eof nil}
     (java.io.PushbackReader.
                       (java.io.InputStreamReader.
                        is "UTF-8")))))

(defn wrap-edn-params
  [handler]
  (fn [req]
    (if-let [body (and (edn-request? req) (:body req))]
      (let [edn-params (binding [*read-eval* false] (-read-edn body))
            req* (assoc req
                   :edn-params edn-params
                   :params (merge (:params req) edn-params))]
        (handler req*))
      (handler req))))
