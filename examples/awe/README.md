# awe

Shows an example of how to create a web-service that handles EDN data via ring-edn.

## Running

Type the following at the command line:

    lein run

The sample will run on port 8080, so be aware if another app is hogging that port.

At another command prompt type the following to test:

```sh
$ curl -X GET http://localhost:8080/

#=> {:hello :cleveland}

$ curl -X PUT -H "Content-Type: application/edn" \ 
  -d '{:name :barnabas}' \
  http://localhost:8080/ 

#=> {:hello :barnabas}%
```

## License

Copyright (C) 2012-2015 Fogus

Distributed under the Eclipse Public License, the same as Clojure.

