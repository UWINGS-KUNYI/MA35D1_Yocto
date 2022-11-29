## The following boards were test in this release

* numaker-iot-ma35d16f90 - only testing on the board

## Using [KAS](https://github.com/siemens/kas) and [Docker](https://docs.docker.com/engine/install/) modern method

first to [install docker engine on host](https://docs.docker.com/engine/install/). And [add your account into docker group for non-root user](https://docs.docker.com/engine/install/linux-postinstall/)

## basic step for build core-image-minimal image
```
 # build core-image-minimal
 $ git clone https://github.com/UWINGS-KUNYI/kas-ma35d1.git --branch=kirkstone
 $ cd kas-ma35d1
 $ ./kas-container build numarker-iot-ma35d1-a1.yaml # default build core-image-minimal target
```
## for customization steps
```
 # if you want build with QT5, try the below command
 $ ./kas-container build numarker-iot-ma35d1-a1.yaml:kas/target-nvt-qt5.yaml

 # want to use bitbake to do something
 $ ./kas-container shell numaker-iot-ma35d1-al.yaml
```

just need to edit .yaml file for control local.conf/bblayers.conf and repositories of meta.

## legacy method to use repo management repositories
## Using repo to download source
```
$ repo init -u https://github.com/UWINGS-KUNYI/MA35D1_Yocto.git -b kirkstone -m meta-ma35d1/base/ma35d1.xml
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

## Build yocto
```
$ DISTRO=nvt-ma35d1-directfb MACHINE=numaker-iot-ma35d16f90 source  sources/init-build-env build
```

###### Usage:
	MACHINE=<machine> DISTRO=<distro> source sources/init-build-env <build-dir>
	<machine>    machine name
	<distro>     distro name
	<build-dir>  build directory

## Step by step to build yocto
To build and use the yocto, do the following:
```
$ repo init -u https://github.com/UWINGS-KUNYI/MA35D1_Yocto.git -b kirkstone -m meta-ma35d1/base/ma35d1.xml
$ repo sync
$ DISTRO=nvt-ma35d1-directfb MACHINE=numaker-iot-ma35d16f90 source  sources/init-build-env build
( Set the device tree of arm-trusted-firmware and the device tree of kernel according to the board
  in source/meta-ma35d1/conf/machine/*.conf )
$ bitbake core-image-minimal

```
