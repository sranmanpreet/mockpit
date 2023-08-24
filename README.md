# Mockpit - Dynamic REST API Mock Server

Mockpit is a versatile and user-friendly REST API mock server that empowers developers to create and manage mock APIs effortlessly. With Mockpit, you can dynamically generate response bodies, leverage path variables and query parameters, and define custom response headers to simulate a wide range of API behaviors. Whether you're testing your frontend application, developing a new feature, or need a temporary API for your project, Mockpit has you covered.

## Features

- **Dynamic Response Generation**: Use JavaScript in ResponseBody evaluation to create dynamic response bodies based on path variables and query parameters.
- **Path Variables and Query Parameters**: Mockpit automatically extracts path variables and query parameters from the request URL and makes them available for dynamic response generation.
- **Custom Response Headers**: Configure response headers for your mock APIs, allowing you to simulate various scenarios.
- **Mock Search**: Quickly locate your mock configurations using Mockpit's search functionality. This feature ensures that you can easily find and work with the mocks you need.
- **Export and Import Mocks**: Seamlessly export and import mock configurations to and from Mockpit. Share your mock setups with colleagues and collaborators to ensure consistency across your team's development efforts.
- **Easy-to-Use Configuration**: Mockpit's intuitive user interface lets you create, edit, and manage mock APIs effortlessly.
- **Fast and Lightweight**: Mockpit is designed to be fast and lightweight, ensuring minimal impact on your development workflow.

## Guides

- [What is Mockpit?](https://github.com/sranmanpreet/mockpit/wiki/What-is-Mockpit)
- [Setting up the application](https://github.com/sranmanpreet/mockpit/wiki/Setting-up-Mockpit)
    - [Native Installation](https://github.com/sranmanpreet/mockpit/wiki/Setup-Mockpit-natively)
    - [Docker Installation](https://github.com/sranmanpreet/mockpit/wiki/Setup-Mockpit-via-Docker)
    - [Docker Compose Installation](https://github.com/sranmanpreet/mockpit/wiki/Setup-Mockpit-via-Docker-Compose)
- [Using Mockpit](https://github.com/sranmanpreet/mockpit/wiki/Using-Mockpit)
    - [Homepage](https://github.com/sranmanpreet/mockpit/wiki/Using-Homepage)
    - [Mock Form](https://github.com/sranmanpreet/mockpit/wiki/Using-Mock-Form)
    - [Manage Mocks](https://github.com/sranmanpreet/mockpit/wiki/Using-Manage-Mocks)
- [Creating Mocks](https://github.com/sranmanpreet/mockpit/wiki/Creating-Mocks)
    - [Create a simple static mock](https://github.com/sranmanpreet/mockpit/wiki/Create-a-simple-static-mock)
    - [Create a dynamic mock with JavaScript](https://github.com/sranmanpreet/mockpit/wiki/Creating-a-dynamic-mock-with-javascript)
    - [Create a dynamic mock using query parameter and path variables with JavaScript](https://github.com/sranmanpreet/mockpit/wiki/Creating-a-dynamic-mock-using-variables-with-javascript)



## Getting Started

### Installation

---
#### Pre-requisites:
- Java 11
- Maven
- Node 14.21
- Angular 15 
- Postgres 15

#### Steps:
1. Clone the Mockpit repository:

   ```
   git clone https://github.com/sranmanpreet/mockpit.git
   ```

2. Install Server:
   
   ```
   cd mockpit/server
   mvn clean install
   
   java -jar target/mockpit-1.0.0-RELEASE.jar
   ```
   
   Server will be deployed on http://localhost:8080 by default. However, port can be changed from application configurations.


3. Install Client:
 
   ```
   cd mockpit/client
   npm install
   npm start 
   ```
   Client will be deployed on http://localhost:4200 by default.
#### Installation using Docker
- Install Server
 ```
docker run -e "SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/mockpitdb" -e "SPRING_DATASOURCE_USERNAME=postgres" -e "SPRING_DATASOURCE_PASSWORD=postgres" -p 8080:8080 mockpit-server:1.0.0-RELEASE
 ```
- Install Client
```
docker run -p 4200:80 -e backendUrl=http://localhost:8080 sranmanpreet/mockpit-client:1.0.0-RELEASE
```
Mockpit will be accessible at http://localhost:4200

#### Installation using Docker Compose
- Run following command at the root of the  project
 ```
docker-compose -f docker-compose.yml up -d
 ```
 
Mockpit will be accessible at http://localhost:4200

### Usage
1. After installation with default configurations, access the Mockpit web interface by navigating to http://localhost:4200 in your web browser.

2. Create and manage mock APIs using the user-friendly interface. Configure dynamic response bodies, path variables, query parameters, and custom response headers as needed.

## Example
Here's a quick example of creating a mock API in Mockpit:

1. Access the Mockpit web interface at http://localhost:4200.
![Homepage](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/homepage.png?raw=true)
2. Click on "New Mock" to define a new mock API.
![New Mock](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/new-mock.png?raw=true)
![New Mock Response](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/new-mock-response.png?raw=true)
3. Configure the URL pattern, dynamic response body using JavaScript, and custom response headers.
![Dynamic Mock](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/new-mock-dynamic.png?raw=true)
4. Save the mock and start using it in your application.
![Dynamic Mock Response](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/new-mock-dynamic-response.png?raw=true)
5. Manage mocks
![Manage Mocks](https://github.com/sranmanpreet/mockpit/blob/master/documentation/assets/manage-mocks.png?raw=true)
### Collaboration
Mockpit welcomes collaboration from developers like you! If you'd like to contribute to the project, feel free to open an issue, submit a pull request, or provide feedback.

To contribute:

1. Fork the Mockpit repository.
2. Create a new branch for your changes.
3. Make your changes and test thoroughly.
4. Submit a pull request with a clear description of your changes.

## License
Mockpit is released under the [MIT License](https://github.com/sranmanpreet/mockpit/blob/master/LICENSE).

---

Mockpit empowers you to easily create, manage, and test mock APIs with dynamic response generation and custom headers. Start using Mockpit today to streamline your development and testing workflows!

[GitHub Repository](https://github.com/sranmanpreet/mockpit)

### Happy mocking! 🚀



