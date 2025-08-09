#!/bin/sh
echo "Waiting for MySQL to be ready..."
until mysqladmin ping -h"mysql" --silent; do
  sleep 2
done
echo "MySQL is up — launching app"
exec java -jar /app.jar                                                                                                                                      3.2s
