#!/sbin/sh
# 
# /system/addon.d/67-topntp.sh
#
. /tmp/backuptool.functions

OUTFD=$(ps | grep -v "grep" | grep -o -E "/tmp/update_binary(.*)" | cut -d " " -f 3);

ui_print() {
  if [ $OUTFD != "" ]; then
    echo "ui_print ${1} " 1>&$OUTFD;
    echo "ui_print " 1>&$OUTFD;
  else
    echo "${1}";
  fi;
}

list_files() {
cat <<EOF
etc/gps.conf
etc/gps/gpsconfig.xml
etc/SuplRootCert
EOF
}

case "$1" in
  backup)
    list_files | while read FILE DUMMY; do
      backup_file $S/$FILE
    done
  ;;
  restore)
    list_files | while read FILE REPLACEMENT; do
      R=""
      [ -n "$REPLACEMENT" ] && R="$S/$REPLACEMENT"
      [ -f "$C/$S/$FILE" ] && restore_file $S/$FILE $R
    done
    ui_print "  Restored TopNTP files";
  ;;
  pre-backup)
    # Stub
  ;;
  post-backup)
    # Stub
  ;;
  pre-restore)
    # Stub
  ;;
  post-restore)
    # Stub
  ;;
esac
