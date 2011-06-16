#!/data/data/com.koushikdutta.timemachine/files/bash

function fail {
    echo $@
    if [ ! -z "$OUTPUT_DIR" ]
    then
        rm -rf $OUTPUT_DIR
    fi
    exit 1
}

function assert {
    VAL=$(eval "echo \$$1")
    echo $VAL
    if [ -z "$VAL" ]
    then
        fail "$1 environment variable not set."
    fi
}

assert FILES_DIR
assert ASSETS_DIR
assert BUSYBOX
assert SQLITE3
assert PACKAGE_NAME
assert OUTPUT_DIR

PACKAGE_APK=$($FILES_DIR/pm path $PACKAGE_NAME | cut -d : -f 2)
if [ ! -f "$PACKAGE_APK" ]
then
  fail PACKAGE_APK: $PACKAGE_APK not found.
fi

$BUSYBOX mkdir -p $ASSETS_DIR

function getmd5 {
  $BUSYBOX md5sum $1 | cut -d ' ' -f 0
}

function saveblob {
  blobmd5=$(getmd5 $1)
  existingblob=$ASSETS_DIR/$blobmd5
  if [ -f "$existingblob" ]
  then
    existingblobmd5=$(getmd5 $existingblob)
    if [ "$blobmd5" != "$existingblob" ]
    then
      $BUSYBOX cp $1 $ASSETS_DIR/$blobmd5
    fi
  else
    $BUSYBOX cp $1 $ASSETS_DIR/$blobmd5
  fi
  echo $blobmd5 > $2
  if [ ! -z "$3" ]
  then
    $BUSYBOX rm -f $1
  fi
}

$BUSYBOX mkdir -p $OUTPUT_DIR
cd /data/data/$PACKAGE_NAME
$BUSYBOX tar czvf $OUTPUT_DIR/internal.tgz .
saveblob $OUTPUT_DIR/internal.tgz $OUTPUT_DIR/internal.md5sum 1
$BUSYBOX cp $OUTPUT_DIR/icon.png $OUTPUT_DIR/..
$BUSYBOX cp $OUTPUT_DIR/metadata.json $OUTPUT_DIR/..

if [ -d /sdcard/Android/data/$PACKAGE_NAME ]
then
  cd /sdcard/Android/data/$PACKAGE_NAME
  $BUSYBOX tar czvf $OUTPUT_DIR/external.tgz .
  saveblob $OUTPUT_DIR/external.tgz $OUTPUT_DIR/external.md5sum 1
fi

saveblob $PACKAGE_APK $OUTPUT_DIR/apk.md5sum

echo Saving Market links. $MARKET_DATABASE
if [ -f "$MARKET_DATABASE" ]
then
	SERVER_STRING_ID=$($SQLITE3 $MARKET_DATABASE "select server_string_id from assets10 where package_name='$PACKAGE_NAME' limit 1")
	if [ ! -z "$SERVER_STRING_ID" ]
	then
		echo Saving server_string_id $SERVER_STRING_ID
		echo $SERVER_STRING_ID > $OUTPUT_DIR/server_string_id
	else
		echo server_string_id for $PACKAGE_NAME not found.
	fi
else
	echo Market Database: $MARKET_DATABASE not found.
fi