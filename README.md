# rescheck_java

Assistant tool for http request testing

## request file format

sample file that has two http requests:

```
GET https://httpbin.org/json
Accept: application/json
---
POST https://httpbin.org/anything

this is a request body
ha ha ha
```

## todo

* sending requests using request entries in db file
* github actions for building and testing. see https://github.com/marketplace/actions/gradle-command
