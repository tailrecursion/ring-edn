(ns ring.middleware.edn)

(defn- edn-request?
  [req]
  (if-let [^String type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?edn" type)))))

(defn wrap-edn-params
  [handler]
  (fn [req]
    (if-let [body (and (edn-request? req) (:body req))]
      (let [bstr (slurp body)
            edn-params (binding [*read-eval* false] (read-string bstr))
            req* (assoc req
                   :edn-params edn-params
                   :params (merge (:params req) edn-params))]
        (handler req*))
      (handler req))))

