# backend-cloud

ChordSked Spring Cloud skeleton project.

## Modules
- chordsked-common
- chordsked-gateway
- chordsked-auth
- chordsked-admin
- chordsked-teacher
- chordsked-student
- chordsked-notification

## Local infrastructure
- docker compose file: deploy/docker/docker-compose.yml
- mysql mapped port: 3307
- redis mapped port: 6380
- kafka mapped port: 9092
- nacos default address: http://localhost:8848

## Service ports
- chordsked-gateway: 8081
- chordsked-auth: 8082
- chordsked-admin: 8083
- chordsked-teacher: 8084
- chordsked-student: 8085
- chordsked-notification: 8086

## Notes
- Compose uses explicit stable versions by default instead of mutable latest tags.
- You can override image versions and ports with environment variables before running docker compose.
- Nacos registration and config are disabled by default. Set NACOS_ENABLED=true to enable them.