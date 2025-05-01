FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY dist/chat-frontend/browser /usr/share/nginx/html
CMD ["nginx", "-g", "daemon off;"]
