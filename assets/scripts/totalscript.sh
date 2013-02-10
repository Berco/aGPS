#!/data/data/by.zatta.agps/files/busybox sh
# script for aGPS

BB="/data/data/by.zatta.agps/files/busybox"
#BB="busybox"

install()
{	
	#mounting /system as rw
	$BB mount | $BB grep "/system" | $BB awk '{system("$BB mount -o rw,remount -t "$5" "$1" "$3"")}'
	
	
	$BB rm /system/etc/gps.conf
	$BB rm /system/etc/SuplRootCert
	$BB cat /data/data/by.zatta.agps/files/gps.conf > /system/etc/gps.conf
	$BB chmod 644 /system/etc/gps.conf
	
	if [ $2 = ssl ]; then
		$BB cat /data/data/by.zatta.agps/files/SuplRootCert > /system/etc/SuplRootCert
		$BB chmod 644 /system/etc/SuplRootCert
	fi
	
	#mounting /system as ro
	$BB mount | $BB grep "/system" | $BB awk '{system("$BB mount -o ro,remount -t "$5" "$1" "$3"")}'
	
	


	if [ $1 = reboot ]; then
		$BB reboot
	fi	
}

configexists()
{	
	# [ -e "/system/etc/gps/gpsconfig.xml" ] && echo TRUE config xml is there
	if [ -e "/system/etc/gps/gpsconfig.xml" ]; then
		$BB mount | $BB grep "/system" | $BB awk '{system("$BB mount -o rw,remount -t "$5" "$1" "$3"")}'
		$BB sed -i 's/PeriodicTimeOutSec.*/PeriodicTimeOutSec="'$1'"/' /system/etc/gps/gpsconfig.xml
		$BB grep -n "PeriodicTimeOutSec" /system/etc/gps/gpsconfig.xml		
		$BB mount | $BB grep "/system" | $BB awk '{system("$BB mount -o ro,remount -t "$5" "$1" "$3"")}'
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
	configexists) configexists $2;;
  esac
done
