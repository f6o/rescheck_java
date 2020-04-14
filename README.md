# rescheck_java

Assistant tool for http request testing

## request file format

A single request chunk is like "Request" defined in RFC2616 Section 5:

```
METHOD URL
HEADER1
HEADER2
...

BODY
...
```

* `METHOD` is `"GET"` or `"POST"`
* `HEADERn` is `NAME ":" WHITESPACE+ VALUE`
* CRLF is allowed for file newline code.
* request chunks are separated by a line with three or more hyphens `---`  

Sample file that has two http requests:

```
GET https://httpbin.org/json
Accept: application/json
---
POST https://httpbin.org/anything

this is a request body
ha ha ha
```

## build automation

using GitHub Actions

* .github/workflows/gradle.yaml
* https://github.com/actions/download-artifact
* https://github.com/marketplace/actions/gradle-command

## todo

* sending requests using request entries in db file
* build fat jar
* github actions for testing
