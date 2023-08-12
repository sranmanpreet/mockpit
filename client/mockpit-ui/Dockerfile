# Stage 1: Build an Angular Docker Image
FROM node:14.21.0-alpine as build
WORKDIR /apps
COPY . .
RUN npm install
COPY . /apps
ARG configuration=production
RUN npm run build --outputPath=/apps/dist/ --configuration=$configuration
# Stage 2, use the compiled app, ready for production with Nginx
FROM nginx:alpine
# COPY /nginx-custom.conf /etc/nginx/conf.d/
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /apps/dist/ /usr/share/nginx/html/
# Copy the EntryPoint
COPY ./entryPoint.sh /
RUN chmod +x entryPoint.sh
ENTRYPOINT ["sh","/entryPoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
