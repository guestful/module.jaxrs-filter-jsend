JAX-RX Filter Jsend
===================

JAX-RX Filter to support a modified [Jsend](http://labs.omniti.com/labs/jsend) body wrapper through a `@Jsend` annotation

Checkout last version [here](https://bintray.com/guestful/maven/guestful.module.jaxrs-filter-jsend/view)

[![Build Status](https://drone.io/github.com/guestful/module.jaxrs-filter-jsend/status.png)](https://drone.io/github.com/guestful/module.jaxrs-filter-jsend/latest)

Setup
-----

Add in your POM:

```
<repositories>
    <repository>
        <id>bintray</id>
        <url>http://dl.bintray.com/guestful/maven</url>
    </repository>
</repositories>
```

and

```
<dependency>
    <groupId>com.guestful.module</groupId>
    <artifactId>guestful.module.jaxrs-filter-jsend</artifactId>
    <version>?</version>
</dependency>
```

In your resouce config:

```
registerClasses(JSendFeature)
```

In your resources, add `@Jsend` annotation like this.

```
@GET
@Path("version")
@Produces("application/json; charset=utf-8")
@Jsend
Map getVersion() {
    return [
        version: Env.VERSION
    ]
}
```

Responses & Errors
------------------

All responses will look roughly like this:

```
{
    "meta": {
        "status": 200
    },
    "error": {
        "type": "<type>",
        "data": {...},
        "message": "an optional string"
    },
    "data": {...}
}
```

 - __data__: JSON response, if any
 - __meta__: Meta information about the request and response
   - __status__: HTTP sattus code. In JSONP, the HTTP status code will always be 200 and `status` will hold the real HTTP status code value
 - __error__: Error section if error:
   - __type__: error type (see below)
   - __data__: error details, optional. I.e., for Bad Requests, contains validation errors.
   - __message__: optional message describing the error. I.e. in case of Internal 500 error, this field could be set.

Here is a list of error `type` and its matching `status` code:

 - status: `401`, type: __authc__: Authentication error.
 - status: `403`, type: __authz__: Although authentication succeeded, the acting user is not allowed to see this information due to privacy restrictions.
 - status: `400`, type: __request__: A required parameter was missing or a parameter was malformed. This is also used if the resource ID in the path is incorrect. Also case where no Access Token is provided.
 - status: `404`, type: __notfound__: The requested path does not exist.
 - status: `405`, type: __method__: The method (GET, PUT, POST, DELETE) set for the request is not allowed for the requested path..
 - status: `200`, type: __deprecated__: Something about this request is using deprecated functionality, or the response format may be about to change.
 - status: `500`, type: __server__: Server is currently experiencing issues. Check [status.guestful.com](http://status.guestful.com) for updates.
 - status: `???`, type: __other__: For any other status code, the type will be other

In case of a `BAD REQUEST`, the response will look like this:

```
{
    "meta": {
        "status": 400
    },
    "error": {
        "type": "request",
        "data": [ {"key": "firstName", "type": "required"}, {"key": "lastName", "type": "invalid"} ],
    }
}
```
