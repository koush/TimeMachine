if [ -z "$FILESDIR" ]
then
  echo FILESDIR environment variable not set.
  exit 1
fi

if [ -z "$BUSYBOX" ]
then
  echo BUSYBOX environment variable not set.
  exit 1
fi

if [ -z "$PACKAGE_NAME" ]
then
  echo PACKAGE_NAME environment variable not set.
  exit 1
fi

if [ -z "$OUTPUT_DIR" ]
then
  echo OUTPUT_DIR environment variable not set.
  exit 1
fi

if [ -z "$PACKAGE_APK" ]
then
  echo PACKAGE_APK environment variable not set.
  exit 1
fi

if [ -z "$TIMEMACHINEDIR" ]
then
  echo TIMEMACHINEDIR environment variable not set.
  exit 1
fi

if [ ! -f $PACKAGE_APK ]
then
  echo PACKAGE_APK: $PACKAGE_APK not found.
  exit 1
fi

$BUSYBOX mkdir -p $OUTPUT_DIR
cd /data/data/$PACKAGE_NAME
$BUSYBOX tar czvf $OUTPUT_DIR/internal.tgz .

if [ -d /sdcard/Android/data/$PACKAGE_NAME ]
then
  cd /sdcard/Android/data/$PACKAGE_NAME
  $BUSYBOX tar czvf $OUTPUT_DIR/external.tgz .
fi

# save space by checking to see if an md5 blob matches
APK_MD5=$($BUSYBOX md5sum $PACKAGE_APK | $BUSYBOX cut -d ' ' -f 0)
TIMEMACHINE_ASSETS=$TIMEMACHINEDIR/assets
EXISTING_APK=$TIMEMACHINE_ASSETS/$APK_MD5
$BUSYBOX mkdir -p $TIMEMACHINE_ASSETS
if [ -f "$EXISTING_APK" ]
then
  EXISTING_APK_MD5=$($BUSYBOX md5sum $EXISTING_APK | $BUSYBOX cut -d ' ' -f 0)
  if [ "$APK_MD5" != "$EXISTING_APK_MD5" ]
  then
    $BUSYBOX cp $PACKAGE_APK $TIMEMACHINE_ASSETS/$APK_MD5
  fi
else
  $BUSYBOX cp $PACKAGE_APK $TIMEMACHINE_ASSETS/$APK_MD5
fi

echo $APK_MD5 > $OUTPUT_DIR/apk.md5sum