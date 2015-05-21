(ns ring.middleware.edn-test
  (:use [ring.middleware.edn])
  (:use [clojure.test])
  (:import java.io.ByteArrayInputStream))

(defn stream [s]
  (ByteArrayInputStream. (.getBytes s "UTF-8")))

(def build-edn-params
  (wrap-edn-params identity))

(deftest noop-with-other-content-type
  (let [req {:content-type "application/xml"
             :body (stream "<xml></xml>")
             :params {"id" 3}}
        resp (build-edn-params req)]
    (is (= "<xml></xml>") (slurp (:body resp)))
    (is (= {"id" 3} (:params resp)))
    (is (nil? (:edn-params resp)))))

(deftest augments-with-edn-content-type
  (let [req {:content-type "application/edn; charset=UTF-8"
             :body (stream "{:foo :bar}")
             :params {"id" 3}}
        resp (build-edn-params req)]
    (is (= {"id" 3 :foo :bar} (:params resp)))
    (is (= {:foo :bar} (:edn-params resp)))))

(deftest augments-with-edn-content-type-no-eval
  (let [req {:content-type "application/edn; charset=UTF-8"
             :body (stream "{:expr (+ 1 2)}")
             :params {"id" 3}}
        resp (build-edn-params req)]
    (is (= {"id" 3 :expr '(+ 1 2)} (:params resp)))
    (is (= '{:expr (+ 1 2)} (:edn-params resp)))))

(deftest augments-with-edn-content-type-no-read-eval
  (let [req {:content-type "application/edn; charset=UTF-8"
             :body (stream "{:expr #=(+ 1 2)}")}]
    (is (thrown? RuntimeException (build-edn-params req)))))

(deftest augments-with-mixed-content-type
  (let [req {:content-type "application/vnd.foobar+edn; charset=UTF-8"
             :body (stream "{:foo :bar}")
             :params {"id" 3}}
        resp (build-edn-params req)]
    (is (= {"id" 3 :foo :bar} (:params resp)))
    (is (= {:foo :bar} (:edn-params resp)))))
