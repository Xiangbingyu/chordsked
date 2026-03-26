FROM node:22.22.1


WORKDIR /app
COPY package*.json ./
COPY node_modules/ ./node_modules/  
COPY . .

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
