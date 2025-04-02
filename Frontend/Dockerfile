# Stage 1: Build the React application
FROM node:16 AS build
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to install dependencies
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps --silent

# Copy the rest of the application code
COPY . .

# Build the React application for production
RUN npm run build

# Stage 2: Serve the application using a lightweight web server
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove the default nginx static files
RUN rm -rf ./*

# Copy the built React application from the previous stage
COPY --from=build /usr/src/app/build .

# Expose port 80 for the web server
EXPOSE 80

# Start nginx server
CMD ["nginx", "-g", "daemon off;"]
