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
    if [ -z "$VAL" ]
    then
        fail "$1 environment variable not set."
    fi
}

assert FILES_DIR
assert BUSYBOX
assert PACKAGE_NAME
assert INPUT_DIR
assert ASSETS_DIR

APK_MD5=$(cat $INPUT_DIR/apk.md5sum)
APK=$ASSETS_DIR/$APK_MD5
if [ ! -f $APK ]
then
	fail $APK not found.
fi

if [ ! -z "APK_EXISTS" ]
then
	if [ -z "$INSTALL_APK" ]
	then
		echo Clearing $PACKAGE_NAME
		pm clear $PACKAGE_NAME
		echo Disabling $PACKAGE_NAME
		pm disable $PACKAGE_NAME
	else
		pm uninstall $PACKAGE_NAME
	fi
fi

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

function restoreblob {
	if [ ! -f "$1" ]
	then
		fail $1 not found.
	fi
	blobmd5=$ASSETS_DIR/$(cat $1)
	if [ ! -f "$blobmd5" ]
	then
		fail $blobmd5 not found.
	fi
	tar xzvf $blobmd5
}

cd $APK_DATA_DIR
echo Restoring data.
restoreblob $INPUT_DIR/internal.md5sum
$BUSYBOX chown -R $PACKAGE_UID:$PACKAGE_UID .

if [ -f $INPUT_DIR/external.md5sum ]
then
	echo Restoring external data.
	$BUSYBOX mkdir -p /sdcard/Android/data/$PACKAGE_NAME
	cd /sdcard/Android/data/$PACKAGE_NAME
	restoreblob $INPUT_DIR/external.md5sum
fi

pm enable $PACKAGE_NAME
