#!/usr/bin/env bash

echo ""
echo ""
echo "********************"
echo "CONFIGURE LOCALES..."
echo "********************"
echo ""
echo ""
DEBIAN_FRONTEND=noninteractive
apt install -y locales
sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen
dpkg-reconfigure --frontend=noninteractive locales
update-locale LANG=en_US.UTF-8
