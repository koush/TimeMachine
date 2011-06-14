#!/data/data/com.koushikdutta.timemachine/files/bash

function fail {
    echo $@
    if [ ! -z "$OUTPUT_DIR" ]
    then
        rm -rf $OUTPUT_DIR
    fi
    # try to reenable it...
    if [ ! -z "$PACKAGE_NAME" ]
    then
	    pm enable $PACKAGE_NAME > /dev/null 2> /dev/null
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

echo FD: $FILES_DIR
assert FILES_DIR
assert BUSYBOX
assert PACKAGE_NAME
assert INPUT_DIR
assert ASSETS_DIR

echo INPUT_DIR: $INPUT_DIR
APK_MD5=$(cat $INPUT_DIR/apk.md5sum)
APK=$ASSETS_DIR/$APK_MD5
if [ ! -f $APK ]
then
	fail $APK not found.
fi

echo Disabling $PACKAGE_NAME
pm disable $PACKAGE_NAME
echo Clearing $PACKAGE_NAME
pm clear $PACKAGE_NAME
if [ ! -z "$INSTALL_APK" ]
then
	echo Reinstalling $PACKAGE_NAME
	$FILES_DIR/pm install -r $FORWARD_LOCK $INSTALLER $INSTALL_LOCATION $APK
	if [ "$?" != "0" -a -z "$SKIP_APK" ]
	then
		fail Package install of $APK failed.
	fi
	echo Reinstallation of $PACKAGE_NAME complete.
fi

APK_DATA_DIR=/data/data/$PACKAGE_NAME
if [ ! -d "$APK_DATA_DIR" ]
then
	fail $APK_DATA_DIR directory not found.
fi

PACKAGE_UID=$(ls -l /data/data | $BUSYBOX grep $PACKAGE_NAME | $BUSYBOX awk '{print $3}')

if [ -z "$PACKAGE_UID" ]
then
	fail Could not determine package uid.
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

pm enable $PACKAGE_NAME