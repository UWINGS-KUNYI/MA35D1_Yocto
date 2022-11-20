# The following boards were test in this release

* numaker-iot-ma35d16f90 - only testing on the board

# Using repo to download source
```
$ repo init -u https://github.com/UWINGS-KUNYI/MA35D1_Yocto.git -b dunfell -m meta-ma35d1/base/ma35d1.xml
$ repo sync
```
###### NOTE:
```
1.Probably you will get server certificate verification failed
Solve it in the following way:
	export GIT_SSL_NO_NOTIFY=1
	or
	git config --global http.sslverify false

2.The setting of the board can be modified at sources/meta-ma35d1/conf/machine/<machine>.conf
```

# Build yocto
```
$ DISTRO=nvt-ma35d1-directfb MACHINE=numaker-iot-ma35d16f90 source  sources/init-build-env build
```

###### Usage:
	MACHINE=<machine> DISTRO=<distro> source sources/init-build-env <build-dir>
	<machine>    machine name
	<distro>     distro name
	<build-dir>  build directory

# Step by step to build yocto
To build and use the yocto, do the following:
```
$ repo init -u https://github.com/UWINGS-KUNYI/MA35D1_Yocto.git -b dunfell -m meta-ma35d1/base/ma35d1.xml
$ repo sync
$ DISTRO=nvt-ma35d1-directfb MACHINE=numaker-iot-ma35d16f90 source  sources/init-build-env build
( Set the device tree of arm-trusted-firmware and the device tree of kernel according to the board
  in source/meta-ma35d1/conf/machine/*.conf )
$ bitbake core-image-minimal

```
