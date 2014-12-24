#!/bin/bash

#Parameters:
#------------------
#1. FSCOMP - type of compression to use with the file system.
#a) zfs - simply on/off. the default compression engine used is lzjb
#(documetnation is here http://open-zfs.org/wiki/Performance_tuning
#b) btrfs - compress=off or compress-force=zlib (or whatever other
#engine you want). compress-force forces aplpying compression to all
#files. in my experience without this mysql files are not compressed.
#2. FS - type of file system, i.e btrfs/ext4/zfs
#3. LOADONLY - TPCC loads data then runs the actual benchmark. this may
#be useless for you, so you can delete this parameter and the relevant
#part in the code which uses it

#How the script works:
#-------------------------------
#(0. shutdown mysql)
#1. given a fresh device in /dev/sda5 (hardcoded, change to whatever you
#need) it mkfs-mounts on it the file system to /var/lib/mysql. this is
#mysql's default data files folder.
#2. copy a fresh copy of that directory, from /home/aviadzuc/orig-mysql
#back to /var/lib/mysql.
#from my experience saving aside this fresh, copying it etc was easy
#and straighforward
#3. chnage permissions etc
#4. restart mysql
#5. run test (in my case this means loading the database, running oltp
#benchmark, etc. you'll need to load your own database, run linkbench
#as you do right now etc.)
#6. when test is done, stop mysql, unmount file system etc
#so i think that with minor changes you can easily use this script. the
#only really important part is before using this script, to drop all
#databases, shutdown mysql and save a fresh copy of /var/lib/mysql to
#some other place in the root file system, so you can re-use it every
#time.

#this should take care of file system compression stuff for you.

#mysql compression is more tricky.  basically you need to follow the
#instructions here
#http://dev.mysql.com/doc/refman/5.6/en/innodb-compression-usage.html
#1) set two global configuration parameters:
#SET GLOBAL innodb_file_per_table=1;
#SET GLOBAL innodb_file_format=Barracuda;
#2) and add:
#ROW_FORMAT=COMPRESSED
#KEY_BLOCK_SIZE=8;
#after every sql statement which creates a table. looking at Linkbench
#code this means the sql statements ones you probably took from the
#README  to run your tests
#https://github.com/facebook/linkbench/blob/master/README.md

#KEY_BLOCK_SIZE determines "how large each compressed chunk is" in
#kilobytes. Since MySQL default page size is 16KB, setting this to 8/4
#makes sense (and 16 is meaningless). I used 4 in my tests.

#The problematic part here is the file format. To enable compression,
#MySQL needs to use a special file format they call Barracuda, which is
#NOT the default (and also set the innodb_file_per_table directive that
#barracuda requires)
#check out this explanation for more details
#http://www.mysqlperformanceblog.com/2014/01/14/innodb-file-formats-here-is-one-pitfall-to-avoid/

#If I remember correctly, using it as they suggest in the documentation
#proved probelematic. You can always try, maybe it'll work for you.
#But as I remember to make it work I had to
#- drop all databases (from command line or whatever)
#- stop mysql
#- erase all ibd* files from /var/lib/mysql
#- set at the bottom of /etc/mysql/my.cnf the relevant statements, i.e.
#innodb_file_per_table = 1
#innodb_file_format = barracuda

#if you dont do this, mysql may simply ignore the settings if you just
#try to do them from command line...
#I remember having to struggle with this a bit to make it work
#previously on my small VM. So if you want support tell me.

#once you get this, you can of course use the file system script (with
#a fresh copy of the /var/lib/mysql after you set barracuda etc) to run
#your test.

FSCOMP=$1
FS=$2 # ext4/zfs/btrfs
LOADONLY=$3 # "load" for laod only. else execute

# check arguments
if [ $# -eq 0 ]
  then
    echo "No arguments supplied"
    exit
fi

#echo "stopping mysql"
#sudo service mysql stop
#sleep 2 

if [ "$LOADONLY" != "load" ]
  then
  echo "dont load"
fi

#echo "current data at /var/lib/mysql/validation"
#ls /var/lib/mysql/validation
#echo "remove data from /var/lib/mysql/validation"
#sudo chown -R mysql:mysql /var/lib/mysql/validation
#sudo rm /var/lib/mysql/validation/* -R
#echo "after delete at /var/lib/mysql/validation"
#ls /var/lib/mysql/
#sleep 2 

#if [ "$FS" == "ext4" ]
#  then
#  ## ext4 ##
#  sudo umount /var/lib/mysql/validation
#  sudo mkfs -t ext4 -b 4096 -F /dev/sda5
#  sudo mount -t ext4 /dev/sda5 /var/lib/mysql/validation
#elif [ "$FS" == "btrfs" ]
#  then
  ## btrfs ##
#  sudo umount /var/lib/mysql/validation
#  sudo mkfs -t btrfs -M /dev/sda5 # mixed metadata and data
#  sudo mount -t btrfs -o ${FSCOMP} /dev/sda5 /var/lib/mysql/validation # compress-force=zlib, compress=off
#elif [ "$FS" == "zfs" ]
#  then
## zfs ## 
  #sudo umount /var/lib/mysql
  #sudo zfs unmount -f /var/lib/mysql
  #sudo zpool destroy data
#  echo "create and mount file system in /var/lib/mysql/"
#  sudo zpool create -f -o ashift=12 -m /var/lib/mysql data /dev/sda5
#  sudo zfs set recordsize=16k data # non-default record size
#  sudo zfs set compression=${FSCOMP} data # on,off
#else
#  echo "invalid FS ${FS}"
#  exit
#fi
#sleep 2 

#echo "Copy the files back from the backup directory at /var/lib/mysql"
#sudo cp /home/user/orig_mysql/* /var/lib/mysql/ -R
#sudo chown -R mysql:mysql /var/lib/mysql
#sleep 2 

#echo "stop apparmor"
#sudo /etc/init.d/apparmor stop;
#sudo /etc/init.d/apparmor teardown;
#sudo update-rc.d -f apparmor remove

echo "restarting mysql..."
sudo service mysql start
sleep 2 

echo "re-creating db"
mysqladmin -u root -proot create linkdb
echo "has the database been created?"
ls /var/lib/mysql
sleep 2 

cd /home/user/workspace/oltpbench

echo "run oltp benchmark"
bash ./oltpbenchmark -b linkbench -c /home/user/workspace/oltpbench/config/sample_linkbench_config.xml --create=true --execute=true -s 1 -o outputfile

#if [ "$LOADONLY" != "load" ]
#  then
#  echo "run oltp benchmark"
#  #vmstat -n 1 1800 > vmstat.csv &
#  bash ./oltpbenchmark -b linkbench -c /home/user/workspace/oltpbench/config/sample_linkbench_config.xml --execute=true -s 1 -o outputfile
#fi
echo "File systems prior DB deletion"
df
ls -lah /var/lib/mysql/linkdb
echo "Compression ratio of db file"
du -hs /var/lib/mysql/linkdb
sleep 2 

echo "dropping db"
/usr/bin/mysql -u root -proot -e "DROP DATABASE linkdb"
sleep 2 

echo "File systems after DB deletion"
df
ls -lah /var/lib/mysql/
sleep 2 

echo "Stopping mysql"
sudo service mysql stop

#if [ "$FS" == "ext4" ]
#  then
  ## btrfs/ext4 ##
#  sudo umount /var/lib/mysql/validation
#elif [ "$FS" == "btrfs" ]
#  then
  ## btrfs/ext4 ##
#  sudo umount /var/lib/mysql/validation
#elif [ "$FS" == "zfs" ]
#  then
  ## zfs ## 
  # destroy zpool, umount, and re-instate old /var/lib/mysql/validation
#  echo "remove zfs"
#  sudo umount /var/lib/mysql
#  sudo zfs unmount -f /var/lib/mysql
#  sudo zpool destroy data
#else
#  echo "invalid FS ${FS}"
#  exit
#fi

