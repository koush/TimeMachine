#!/data/data/com.koushikdutta.timemachine/files/bash

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

if [ -z "$TIMEMACHINEDIR" ]
then
  echo TIMEMACHINEDIR environment variable not set.
  exit 1
fi

PACKAGE_APK=$($FILESDIR/pm path $PACKAGE_NAME | cut -d : -f 2)
if [ ! -f $PACKAGE_APK ]
then
  echo PACKAGE_APK: $PACKAGE_APK not found.
  exit 1
fi

TIMEMACHINE_ASSETS=$TIMEMACHINEDIR/assets
$BUSYBOX mkdir -p $TIMEMACHINE_ASSETS

function getmd5 {
  $BUSYBOX md5sum $1 | cut -d ' ' -f 0
}

function saveblob {
  blobmd5=$(getmd5 $1)
  existingblob=$TIMEMACHINE_ASSETS/$blobmd5
  if [ -f "$existingblob" ]
  then
    existingblobmd5=$(getmd5 $existingblob)
    if [ "$blobmd5" != "$existingblob" ]
    then
      $BUSYBOX cp $1 $TIMEMACHINE_ASSETS/$blobmd5
    fi
  else
    $BUSYBOX cp $1 $TIMEMACHINE_ASSETS/$blobmd5
  fi
  echo $blobmd5 > $2
}

$BUSYBOX mkdir -p $OUTPUT_DIR
cd /data/data/$PACKAGE_NAME
$BUSYBOX tar czvf $OUTPUT_DIR/internal.tgz .
saveblob $OUTPUT_DIR/internal.tgz $OUTPUT_DIR/internal.md5sum
rm -f $OUTPUT_DIR/internal.tgz

if [ -d /sdcard/Android/data/$PACKAGE_NAME ]
then
  cd /sdcard/Android/data/$PACKAGE_NAME
  $BUSYBOX tar czvf $OUTPUT_DIR/external.tgz .
  saveblob $OUTPUT_DIR/external.tgz $OUTPUT_DIR/external.md5sum
  rm -f $OUTPUT_DIR/external.tgz
fi

# save space by checking to see if an md5 blob matches
saveblob $PACKAGE_APK $OUTPUT_DIR/apk.md5sum