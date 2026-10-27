#!/bin/sh
# Runs on helios: installs the uploaded files and restarts the Java server and Apache.
set -e
BASE="$HOME/web-programing"
NEW="$BASE/deploy"
FCGI_PORT=24921

cp -R "$NEW/www/." "$BASE/httpd-root/www/"
cp "$NEW/lab2-server.jar" "$BASE/lab2-server/"

cd "$BASE/lab2-server"
java -version 2>&1 | head -1
pkill -u "$(whoami)" -f "FCGI_PORT=$FCGI_PORT" || true
sleep 1
nohup java -DFCGI_PORT=$FCGI_PORT -jar lab2-server.jar > server.log 2>&1 < /dev/null &
sleep 2
if pgrep -u "$(whoami)" -f "FCGI_PORT=$FCGI_PORT" > /dev/null; then
    echo "Java server running on port $FCGI_PORT"
else
    echo "Java server did not start:"
    cat server.log
    exit 1
fi

httpd -f "$BASE/httpd-root/httpd.conf" -k restart
rm -rf "$NEW"
echo "Done. Open http://localhost:8080/html/index.html through the SSH tunnel"
