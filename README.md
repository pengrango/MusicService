# MusicService

Welcome to MusicService! This guide provides essential information about our project.

## Requirements

- Java 21
- Maven 3.9.x

## Getting Started

**Build the Project:**
mvn clean install

**Run the Application:**
mvn spring-boot:run


## Implementation Highlights

Our application leverages Spring Boot WebFlux for efficient handling of high loads, utilizing reactive programming and non-blocking I/O. Key features include:

- **Resiliency**: .
- **Scalability**: Ideal for cloud environments (e.g., AWS ECS), supporting auto-scaling.
- **Performance**: Uses Hazelcast, an in-memory, distributed cache, to speed up response times and reduce external service load. Key benefits include synchronized data across nodes and good stability during maintenance work such as rolling update in ECS.

## Testing

The project includes both unit and integration tests. Mocked objects are used for unit testing, while `WebMockServer` mocks external dependencies for integration testing.
Below are some mbIds to used in manual test.

| UUID                                 | Comments                                                     |
|--------------------------------------|--------------------------------------------------------------|
| b83bc61f-8451-4a5d-8b8e-7e9ed295e822 | an image url is missing                                      |
| 36c98fe2-02f6-44d1-aa1f-0cdf7eb48289 | Seems not a real people                                      |
| 54b8f3d4-06f9-4729-a141-5039ca7af9a0 | No release group                                             |
| 85773020-07a1-46a4-8436-cc7ae8bf698f | No disambiguation                                            |
| 2f3c4d70-0462-40da-bba3-0aec5772c556 | No images for some albums                                    |
| 25f3abd9-63b5-471a-bd25-feb9672dfa11 |                                                              |
| f8dc40b0-9db2-4954-a355-d5c44c5c6bc2 | very little data                                             |
| c9b1045e-e4c5-47a1-8a6f-794d64c9c9b7 | no wikidata, no release groups, so no albums and description |
| baff466c-be65-4425-85bf-56069801d947 | Special japanese character                                   |
| 19ab6264-9a5e-441c-a756-bb9a086f1b1a | Seems a group with small amount of data.                     |
| ffb77292-9712-4d03-94aa-bdb1d4771d38 | Special Swedish chraracters                                  |
| 692e367d-2846-442d-b13d-1177c3681c65 | Special Chinese chracrcters                                  |


## Future Enhancements

- Configurations via Environment Variables.
- Containerization with Docker for cloud deployment.
- JVM tuning for optimized performance.
- Enhanced caching strategies, possibly integrating Redis.
