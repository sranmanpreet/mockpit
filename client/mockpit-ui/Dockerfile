FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY ./dist/mockpit-ui /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 4200

CMD ["nginx", "-g", "daemon off;"]