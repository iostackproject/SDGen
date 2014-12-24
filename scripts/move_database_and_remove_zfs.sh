sudo mv /var/lib/mysql/* /home/user/Desktop/zfs/
sudo umount /var/lib/mysql
sudo zpool destroy data
mv /home/user/Desktop/zfs/* /var/lib/mysql/
#innodb_use_native_aio=0
#innode_file_per_table=1
