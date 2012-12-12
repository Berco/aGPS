#!/system/xbin/sh
# script for aGPS

install()
{	
	#mounting /system as rw
	mount | grep "/system" | awk '{system("mount -o rw,remount -t "$3" "$1" "$2"")}'
	
	busybox rm /system/etc/gps.conf
	busybox cat /data/data/by.zatta.agps/files/gps.conf > /system/etc/gps.conf
	busybox chmod 644 /system/etc/gps.conf
	
	if [ $2 = ssl ]; then
		busybox cat /data/data/by.zatta.agps/files/SuplRootCert > /system/etc/SuplRootCert
		busybox chmod 644 /system/etc/SuplRootCert
	fi
	
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
	install) install $2 $3;;
	backup) backup;;
  esac
done
