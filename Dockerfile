# Use the nginx image as the base
FROM nginx:alpine

# Copy the nginx.conf file to the container
COPY nginx.conf /etc/nginx/nginx.conf

# Copy the built frontend and backend images to the nginx directory
COPY --from=sranmanpreet/mockpit-client:1.1.0-RELEASE /usr/share/nginx/html /usr/share/nginx/html/
COPY ./entryPoint.sh /
COPY --from=sranmanpreet/mockpit-server:1.1.0-RELEASE /app/mockpit.jar /app/mockpit-server.jar

EXPOSE 80

# Install OpenJDK and other necessary packages
RUN apk update && apk add openjdk11

# Set up environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk
ENV PATH="$JAVA_HOME/bin:${PATH}"

# Start Nginx and Spring Boot app
RUN chmod +x entryPoint.sh
ENTRYPOINT ["sh","/entryPoint.sh"]
#CMD nginx && java -jar /app/mockpit-server.jar
