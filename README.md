# url-shortner
A URL shortener + analytics platform built to practice fast Java API patterns — caching, async processing with Kafka, keyset pagination, resilience, and observability using Spring Boot 4.1 and Java 25.

Actuator links:

Check the cache hit/miss counters:
Cache hits:
http://localhost:8080/actuator/metrics/cache.gets?tag=cache:urlLookup&tag=result:hit
Cache missed:
http://localhost:8080/actuator/metrics/cache.gets?tag=cache:urlLookup&tag=result:miss

Check the raw cache contents:
http://localhost:8080/actuator/caches/urlLookup

Confirm the circuit breaker's baseline state:
http://localhost:8080/actuator/health

http://localhost:8080/actuator/circuitbreakerevents


Where we stand against the original list of 10
#   Practice    Status
1   Virtual threads ✅ (Spring Boot 4 defaults, matches Java 25)
2   Avoid N+1   ✅ (lean entity, open-in-view: false)
3   Tune connection pool    Not yet done explicitly
4   Cache the hot path  ✅ verified just now
5   DTOs over entities  ✅
6   Paginate    Repository method built (findPageByCursor), not yet exposed via controller
7   Async/Kafka ✅ verified
8   GraalVM native image    Not yet done
9   Circuit breaker/resilience  ✅ verified
10  Instrumentation ✅ just finished


Dependency          Boot 4.1 name (don't use)       Boot 3.5.0 name (use this)
Web               spring-boot-starter-webmvc       spring-boot-starter-web
Kafka             spring-boot-starter-kafka        spring-kafka (raw library, no dedicated Boot starter existed pre-4.0)
AOP              spring-boot-starter-aspectj       spring-boot-starter-aop
Resilience4j    resilience4j-spring-boot4          resilience4j-spring-boot3
