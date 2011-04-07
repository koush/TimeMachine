function fail
{
    echo $1 > /data/data/com.koushikdutta.timemachine/error.txt
    exit 1
}

if [ -z "$FILESDIR" ]
then
    echo FILESDIR environment variable not set.
    exit 1
fi

if [ -z "$BUSYBOX" ]
then
    fail BUSYBOX environment varialbe not set.
fi

cd $($BUSYBOX dirname $0)

