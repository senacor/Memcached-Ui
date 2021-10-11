# Memcached-UI

---
# Table of Contents

- [About The Project](#about-the-project)
  * [Built With](#built-with)
- [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
- [Usage](#usage)
  * [Hosts](#hosts)
  * [Key Structure](#key-structure)
- [How to Test](#how-to-test)
- [Contact](#contact)

---

## About The Project

This project provides a simple web-based GUI for [Memcached](https://memcached.org/).
It allows to view the stored key-value pairs and statistics, delete single entries,
delete entries based on their namespace, or flush the entire cache.
> Note: Memcached itself does not support the concept of namespaces or any hierarchical
> structuring of keys. Dividing keys into namespaces is a feature of this application to
> to create a better overview of the cache entries.

### Built With

* [Java Spring Boot](https://spring.io/projects/spring-boot)
* [Thymeleaf](https://www.thymeleaf.org/)

## Getting Started

The simplest way to start the service is to build the docker image and start the memcached-ui
container.

### Prerequisites

* Docker
* Docker Compose and Python 3 (for testing)
* Memcached Instance

### Installation

1. Clone the project to your local machine
```sh
   git clone https://github.com/senacor/Memcached-Ui
```
2. Navigate into the project directory
```sh
   cd memcached-ui
```

3. Build the Docker image using the `build` command
```sh
   docker build -t memcached-ui -f Dockerfile_local .
```

## Usage

If you have a Memcached instance running on your local machine on the default port 11211,
and you just want to manage the key-value pairs stored in that instance, you can
just start the docker container using the following command
```sh
   docker run -p 8080:8080 memcached-ui
```
You can then go into your browser and open http://localhost:8080. You should see the
main page with a single default namespace.
However, you can configure the service in a number of ways described in the following
sections.

### Hosts
By default, the server expects the memcached instance to be running on localhost on
port 11211. If your instance runs under a different address, you can specify it in two
ways, by adjusting the properties `application.yaml` file, or by passing the value as an
environment variable while starting the docker container.

To change the properties file, navigate into the `src/main/resources` directory and
open the file `application.yaml`. Replace the entry under `memcached.hosts` with your
address.

The pass the value as an environment variable, you can use the following command and
define your host
```sh
    docker run -p 8080:8080 -e MEMCACHED_HOSTS=<your-host:port> memcached-ui
```

You can also provide more multiple hosts by separating them with a comma.
```sh
    docker run -p 8080:8080 -e MEMCACHED_HOSTS=<host1:port1>,<host2:port2> memcached-ui
```
> Note: If you specify multiple hosts, the server will treat them as one instance
> and combine the containing entries. To manage each instance individually, you need
> to start multiple docker containers.

### Key Structure
You can also define a custom key structure, which will allow you to separate the keys
into namespaces, use your custom timestamp, or remove unwanted substrings from the key.

To define your custom structure, use the `memcached-ui.key.structure` property or the
`MEMCACHED-UI_KEY_STRUCTURE` environment variable. The following is an example of how
a custom key structure could look like:
```
    unwanted-substring:NAMESPACE:TIMESTAMP:KEY
```
* The `KEY` tag will be used as the actual key name in the keys view. Everything
  else won't be part of the name. The default key structure is a single `KEY` tag.
* The `TIMESTAMP` tag can be used, if your keys contain a timestamp in milliseconds which
  you want to use instead of the default Memcached timestamp.
* Finally, the `NAMESPACE` tag is used, to separate the keys into namespaces. This can
  be usefull, if your keys are divided into logical groups.
* The `unwanted-substring` can be an automatically generated prefix, that has nothing
  to do with the actual value and should be ignored in the keys view.
* The tags should be separated with a non-letter and non-digit character like `:`.
  Otherwise, the server might not be able to distinguish the corresponding tag values.

For the example key structure above valid keys could be
```
    unwanted-substring:namespace-1:1633622135173:key-1
    unwanted-substring:namespace-1:1633622137173:key-2
    unwanted-substring:namespace-2:1633622139173:key-3
```
The keys will then be divided into two namespaces, `namespace-1` and `namespace-2`,
and the key names will be `key-1`, `key-2`, and `key-3` inside the corresponding
namespace.

If some of the keys do not match the custom structure, they will be put into the default
namespace with the full key name e.g.
```
    unwanted-substring:namespace-1:key-4
```
will be stored in the default namespace with `unwanted-substring:namespace-1:key-4` as
key name.

## How to Test

If you want to test the service, you can use [Docker Compose](https://docs.docker.com/compose/)
to start a Memcached-UI service and a local instance of Memcached. For that navigate into the
memcached-ui directory and run
```sh
  docker-compose up
```
This will start a Memcached instance and the Memcached-UI service each inside a Docker container.
The Python script `populate.py` is then executed to write random key-value pairs into the
Memcached instance. You can adjust certain values like the number of keys or the key/value length
inside the script located inside the python directory. Don't forget to rebuild the images if
you make changes to the Python script after building the images.
```sh
  docker-compose up --build
```
After starting the services go into your browser and open http://localhost:8080.


## Contact

Salman Alhadziev - salman.alhadziev@senacor.com
