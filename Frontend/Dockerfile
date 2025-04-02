# Build stage
FROM node:16 AS build
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install --legacy-peer-deps --silent
COPY . .
RUN npm run build

# Serve stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=build /usr/src/app/build .
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
