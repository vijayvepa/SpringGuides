docker compose down

rm -rf ${PWD}/db-data
rm -rf ${PWD}/pgadmin-data


echo "---"

cat .env

echo ""
echo "---"
echo "DEFAULT_EMAIL: $DEFAULT_EMAIL"
echo "DEFAULT_PASSWORD: $DEFAULT_PASSWORD"
echo "DATABASE_USERNAME:$DATABASE_USERNAME"
echo "DATABASE_PASSWORD:$DATABASE_PASSWORD"
echo "DATABASE_NAME: $DATABASE_NAME"


docker compose up -d