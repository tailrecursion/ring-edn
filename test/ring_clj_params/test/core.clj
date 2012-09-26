(ns ring-clj-params.test.core
  (:use [ring.middleware.clj-params])
  (:use [clojure.test])
  (:import java.io.ByteArrayInputStream))

(defn stream [s]
  (ByteArrayInputStream. (.getBytes s "UTF-8")))

(def build-clj-params
  (wrap-clj-params identity))

(deftest noop-with-other-content-type
  (let [req {:content-type "application/xml"
             :body (stream "<xml></xml>")
             :params {"id" 3}}
        resp (build-clj-params req)]
    (is (= "<xml></xml>") (slurp (:body resp)))
    (is (= {"id" 3} (:params resp)))
    (is (nil? (:clj-params resp)))))

(deftest augments-with-clj-content-type
  (let [req {:content-type "application/clojure; charset=UTF-8"
             :body (stream "{:foo :bar}")
             :params {"id" 3}}
        resp (build-clj-params req)]
    (is (= {"id" 3 :foo :bar} (:params resp)))
    (is (= {:foo :bar} (:clj-params resp)))))

(deftest augments-with-clj-content-type-no-eval
  (let [req {:content-type "application/clojure; charset=UTF-8"
             :body (stream "{:expr (+ 1 2)}")
             :params {"id" 3}}
        resp (build-clj-params req)]
    (is (= {"id" 3 :expr '(+ 1 2)} (:params resp)))
    (is (= '{:expr (+ 1 2)} (:clj-params resp)))))

(deftest augments-with-clj-content-type-no-read-eval
  (let [req {:content-type "application/clojure; charset=UTF-8"
             :body (stream "{:expr #=(+ 1 2)}")}]
    (is (thrown? RuntimeException (build-clj-params req)))))

(deftest augments-with-vnd-json-content-type
  (let [req {:content-type "application/vnd.foobar+clojure; charset=UTF-8"
             :body (stream "{:foo :bar}")
             :params {"id" 3}}
        resp (build-clj-params req)]
    (is (= {"id" 3 :foo :bar} (:params resp)))
    (is (= {:foo :bar} (:clj-params resp)))))
