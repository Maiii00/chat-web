FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY dist/chat-frontend/browser/ /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

CMD ["nginx", "-g", "daemon off;"]
