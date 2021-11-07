## Koin Serialization with Kryo

Supplementary example for https://github.com/EsotericSoftware/kryo/issues/864


The repo contains multiple examples, where I have tried to narrow down the issue.
* KryoLambda.kt - generic kotlin labmda, **works** in both ways with kryo
* KryoDefinition.kt - generic lambda, wrapped into other types **works** in both ways with kryo (includes KryoLambda)
* KryoBeanDefinition.kt - more complex wrapper around KryoDefinition, **fails to work with kryo**
* KryoInstanceFactory.kt - Includes KryoBeanDefinition, but does not work.

* These examples arose from my original usecase, which is serializing/deserializing an instance of https://insert-koin.io/
