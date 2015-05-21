# ring-edn

A [Ring](https://github.com/mmcgrana/ring) middleware that augments :params by parsing a request body as [Extensible Data Notation](https://github.com/edn-format/edn) (EDN).

## Where

  * [Source repository](https://github.com/fogus/ring-edn) *-- patches welcomed*

## Usage

### Leiningen

In your `:dependencies` section add the following:

    [fogus/ring-edn "0.2.0"]

### Ring

*the [examples directory of the ring-edn project](http://github.com/fogus/ring-edn/tree/master/examples/awe) contains the source for the following*

To use this middleware using Ring and [Compojure](https://github.com/weavejester/compojure), create a new Leiningen project with a `project.clj` file of the form:

```clojure
(defproject awesomeness "0.0.1"
  :description "true power awesomeness"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring "1.0.2"]
                 [compojure "1.0.1"]
                 [fogus/ring-edn "0.2.0"]]
  :main awesome-app)
```

Next, create a file in `src` called `my_awesome_service.clj` with the following:

```clojure
(ns my-awesome-service
  (:use compojure.core)
  (:use ring.middleware.edn))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})
  
(defroutes handler
  (GET "/" []
       (generate-response {:hello :cleveland}))

  (PUT "/" [name]
       (generate-response {:hello name})))

(def app
  (-> handler
      wrap-edn-params))
```

And finally, create another file in `src` named `awesome_app.clj` with the following:

```clojure
(ns awesome-app
  (:use ring.adapter.jetty)
  (:require [my-awesome-service :as awe]))

(defn -main
  [& args]
  (run-jetty #'awe/app {:port 8080}))
```

### Using custom types

EDN offers extensible types through
[tagged literals](https://github.com/edn-format/edn#tagged-elements)
and `ring-edn` can read those types from the incoming requests.
As an example, let's add `uri` to EDN. In our Clojure program
it will be represented by `java.net.URI` but in other platforms it
might be represented differently, i.e `goog.Uri` in ClojureScript. To
use a new type, we need to define a reader (takes a string and returns
our representation) and a printer (takes our representation and writes
it as a string). The printer determines the tagged literal and it is
implemented as a multimethod of `clojure.core/print-method`. We might
be tempted to use `#uri` for the tagged literal but it needs to be
namespaced in case an application needs to deal with multiple `uri`
representations. Therefore we will use `#my-app/uri`:

```clj
(ns my-app.uri
  (:import (java.net URI)))

(defn read-uri [s]
  (URI. s))

(defmethod print-method java.net.URI [this w]
  (.write w "#my-app/uri \"")
  (.write w (.toString this))
  (.write w "\""))
```

Now we indicate `wrap-edn-params` that whenever it finds `#my-app/uri`
it should read the expression that follows with `read-uri`:

```
(def app
  (-> handler
      (wrap-edn-params {:readers {'my-app/uri #'my-app.uri/read-uri}})))
```

Other options besides `:readers` can be passed to `wrap-edn-params`
which are forwarded to `clojure.edn/read-string` as defined
[here](https://clojure.github.io/clojure/clojure.edn-api.html).


### Testing

Run this app in your console with `lein run` and test with `curl` using the following:

```sh
$ curl -X GET http://localhost:8080/

#=> {:hello :cleveland}

$ curl -X PUT -H "Content-Type: application/edn" \ 
  -d '{:name :barnabas}' \
  http://localhost:8080/ 

#=> {:hello :barnabas}%
```

You can also run the test suite with `lein test`.

## Acknowledgment(s)

Thanks to [Mark McGranaghan](http://markmcgranaghan.com/) for his work on Ring and [ring-json-params](https://github.com/mmcgrana/ring-json-params) on which this project was based.

## License

Copyright (C) 2012-2015 Fogus

Distributed under the Eclipse Public License, the same as Clojure.
