sudo apt-get remove mysql-server mysql-client mysql-common -y
sudo apt-get purge mysql-server mysql-client mysql-common -y
sudo apt-get autoremove -y
sudo apt-get remove --purge mysql\* -y
sudo dpkg -l | grep -i mysql
sudo apt-get clean
sudo updatedb
rm -r /var/log/mysql
rm -r /var/lib/mysql
rm -r /etc/mysql
