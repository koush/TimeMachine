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

if [ -z "$INPUT_DIR" ]
then
  echo INPUT_DIR environment variable not set.
  exit 1
fi

if [ -z "$TIMEMACHINEDIR" ]
then
  echo TIMEMACHINEDIR environment variable not set.
  exit 1
fi

APK_MD5=$(cat $INPUT_DIR/apk.md5sum)
APK=$TIMEMACHINEDIR/assets/$APK_MD5
if [ ! -f $APK ]
then
	echo APK: $APK not found.
	exit 1
fi

$FILESDIR/pm install $APK
if [ "$?" != "0" ]
then
	echo Package install of $APK failed.
fi

APK_DATA_DIR=/data/data/$PACKAGE_NAME
if [ ! -d "$APK_DATA_DIR" ]
then
	echo $APK_DATA_DIR directory not found.
	exit 1
fi

PACKAGE_UID=$(ls -l /data/data | $BUSYBOX grep $PACKAGE_NAME | $BUSYBOX awk '{print $3}')

if [ -z "$PACKAGE_UID" ]
then
	echo Could not determine package uid.
	exit 1
fi

cd $APK_DATA_DIR
$BUSYBOX tar xzvf $INPUT_DIR/internal.tgz
$BUSYBOX chown -R $PACKAGE_UID:$PACKAGE_UID .

if [ -f $INPUT_DIR/external.tgz ]
then
  $BUSYBOX mkdir -p /sdcard/Android/data/$PACKAGE_NAME
  cd /sdcard/Android/data/$PACKAGE_NAME
  $BUSYBOX tar xzvf $INPUT_DIR/external.tgz
fi
