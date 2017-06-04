# CarAds
CardAds allows to put car advertisements to a persisitent storage and to get the ads back via REST API. To run it with default settings (on localhost:8080 with local dynamo db):
```
$ sbt run 'carads.Service'
```
All other settings are in application.conf

## Quick start
```
# Puts new ad
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/put\
    -d '{
            "id": 13,
            "title": "Mercedes",
            "fuel": "Gasoline",
            "price": 60000,
            "new": false,
            "mileage": 100000,
            "registration": "2010-11-01"
        }'
>>> {"isSuccess":true}        

# Gets an existing ad by its id
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/get\
    -d '{
            "id": 13
        }'
>>> {"isSuccess":true,"record":{"id":13,"title":"Mercedes","fuel":"Gasoline","price":60000,"new":false,"mileage":100000,"registration":"2010-11-01"}}

# Modify a field in the ad
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/modify\
    -d '{
            "id": 13,
            "price": 65000
        }'
>>> {"isSuccess":true}

# Adding a few ads
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/put\
    -d '{"id": 14, "title": "MAN", "fuel": "Diesel", "price": 80000, "new": true }'
>>> {"isSuccess":true}
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/put\
    -d '{"id": 15, "title": "BMW", "fuel": "Gasoline", "price": 30000, "new": false, "mileage":80000, "registration":"2009-01-19" }'
>>> {"isSuccess":true}

# Getting of all ads sorted by price
$ curl -X POST -H "Content-Type: application/json"\
    http://localhost:8080/all\
    -d '{"sortby": "price", "limit": 10}'
>>> {"isSuccess":true,"records":[
    {"id":15,"title":"BMW","fuel":"Gasoline","price":30000,"new":false,"mileage":80000,"registration":"2009-01-19"},
    {"id":13,"title":"Mercedes","fuel":"Gasoline","price":60000,"new":false,"mileage":100000,"registration":"2010-11-01"},
    {"id":14,"title":"MAN","fuel":"Diesel","price":80000,"new":true}]}

# Delete an ad
$ curl -X POST -H "Content-Type: application/json" http://localhost:8080/delete -d '{"id": 13}'
>>> {"isSuccess":true}
```
