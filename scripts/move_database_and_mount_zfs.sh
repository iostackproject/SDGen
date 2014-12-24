sudo mv /var/lib/mysql/* /home/user/Desktop/zfs/
sudo zpool create -f -o ashift=12 -m /var/lib/mysql data /dev/sda5
sudo zfs set recordsize=16k data # non-default record size
sudo zfs set compression=on data # on,off
sudo mv /home/user/Desktop/zfs/* /var/lib/mysql/
chmod -R 777 /var/lib/mysql/
chown -R mysql:mysql /var/lib/mysql/


