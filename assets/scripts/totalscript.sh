#!/system/xbin/sh
# script for aGPS

install()
{	
	#mounting /system as rw
	mount | grep "/system" | awk '{system("mount -o rw,remount -t "$3" "$1" "$2"")}'

	#busybox cat /data/data/by.zatta.datafix/files/move_cache.txt > /data/local/datafix/move_cache.txt
	#busybox chmod 740 /data/local/datafix/move_cache.txt
	
	#busybox cat /data/data/by.zatta.datafix/files/skip_apps.txt > /data/local/datafix/skip_apps.txt
	#busybox chmod 740 /data/local/datafix/skip_apps.txt
	
	#mounting /system as ro
	mount | grep "/system" | awk '{system("mount -o ro,remount -t "$3" "$1" "$2"")}'

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
