#!/bin/bash

openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout private_key.pem -out certificate.pem -nodes -subj "/CN=localhost/C=XX/ST=New-York/L=New-York"