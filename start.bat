docker run --name mockpit -e "SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/mockpitdb" -e "SPRING_DATASOURCE_USERNAME=postgres" -e "SPRING_DATASOURCE_PASSWORD=postgres" -e "backendUrl=http://localhost:8080" -p 3000:80 -p 8080:8080 sranmanpreet/mockpit:1.1.0-RELEASE -d