# Choices, important information

## integration data tests

I choose not to use transactional integration tests that automatically rollback changes. 
Instead I do this in each test

```java
@DataJpaTest
// this is to prevent automatic, rolled back transactions
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
// this is to fix problem that lack of automatic, rolled back transactions causes
// it reloads context which reloads H2, which clear any data remaining from previous tests
// Yes, I know it makes tests longer
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaUserRepositoryTest {
}
```
Reason? It gives me greater control over transactions in tests.  

# Here is a list of things to be done which I left to make it faster now

## current com.volume.payments.shared should be com.volume.shared

I had problems running integration tests with expected location, so I just moved it for now.

## Add tests for optimistic locking to base aggregate and entity classes

## Very good validations on domain/entity level

1. via all value objects
2. via policies for given transfer/operation type


## Very good validations on dto level

Using the same basic validators we need to add validation that will report problems to client in a beatifuly simple and descriptive way

This includes:
- very good messages
- not exposing sensitive information in error responses
- some domain exceptions hierarchy
- some good exceptions for external calls e.g. Yapily
- some good handlers for DomainExceptions
- some good handlers for external call errors


