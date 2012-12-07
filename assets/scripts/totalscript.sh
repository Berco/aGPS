#!/system/xbin/sh
# script for aGPS

install()
{
	echo "$1"
	
	# I need a way of mounting /system in rw that works for all various devices

	#busybox cat /data/data/by.zatta.datafix/files/move_cache.txt > /data/local/datafix/move_cache.txt
	#busybox chmod 740 /data/local/datafix/move_cache.txt
	
	#busybox cat /data/data/by.zatta.datafix/files/skip_apps.txt > /data/local/datafix/skip_apps.txt
	#busybox chmod 740 /data/local/datafix/skip_apps.txt
		
	if [ $1 = reboot ]; then
		echo "$1"
		#reboot
	else
		echo "$1"
	fi
	
}

backup()
{
	echo "backup"
}

for i
do
  case "$i" in
	install) install $2;;
	backup) backup;;
  esac
done
