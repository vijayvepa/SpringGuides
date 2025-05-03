set -x

docker compose down

rm -rf ${PWD}/db-data
rm -rf ${PWD}/pgadmin-data


cat .env

echo $DEFAULT_EMAIL
echo $DEFAULT_PASSWORD

docker compose up -d