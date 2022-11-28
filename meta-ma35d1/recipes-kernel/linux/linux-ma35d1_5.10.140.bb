# Copyright (C) 2019-2022
# Copyright 2019-2022 Nuvoton
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Linux Kernel provided and supported by Nuvoton"
DESCRIPTION = "Linux Kernel provided and supported by Nuvoton ma35d1"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel

DEPENDS:append = " \
    libyaml-native \
    openssl-native \
    util-linux-native \
"

# We need to pass it as param since kernel might support more then one
# machine, with different entry points
ma35d1_KERNEL_LOADADDR = "0x80080000"
KERNEL_EXTRA_ARGS += "LOADADDR=${ma35d1_KERNEL_LOADADDR}"

SRCBRANCH = "master"
LOCALVERSION = "-${SRCBRANCH}"
KERNEL_SRCREV ?= "67bfa6069b3ec4bd084eb817691c3eea8f9eb40d"
KERNEL_SRC = "git://github.com/OpenNuvoton/MA35D1_linux-5.10.y.git;protocol=https;branch=${SRCBRANCH}"
SRC_URI:ma35d1 = "${KERNEL_SRC}"
SRC_URI:append:ma35d1 = " \
    file://optee.config \
    file://cfg80211.config \
   ${@bb.utils.contains('DISTRO_FEATURES', '88x2bu', ' file://88x2bu.ko', '', d)} \
"
SRCREV:ma35d1 = "${KERNEL_SRCREV}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# =========================================================================
# Kernel
# =========================================================================
# Kernel image type
KERNEL_IMAGETYPE = "Image"

do_configure:prepend:ma35d1() {
    bbnote "Copying defconfig"
    cp ${S}/arch/${ARCH}/configs/${KERNEL_DEFCONFIG} ${WORKDIR}/defconfig
    cat ${WORKDIR}/cfg80211.config >> ${WORKDIR}/defconfig

    if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
        cat ${WORKDIR}/optee.config >> ${WORKDIR}/defconfig
    fi
}

do_deploy:append:ma35d1() {
    for dtbf in ${KERNEL_DEVICETREE}; do
        dtb=`normalize_dtb "$dtbf"`
        dtb_ext=${dtb##*.}
        dtb_base_name=`basename $dtb .$dtb_ext`
        ln -sf $dtb_base_name.dtb ${DEPLOYDIR}/Image.dtb
    done
}

do_install:append:ma35d1() {
    if [ -e ${WORKDIR}/88x2bu.ko ]; then
        install -d ${D}/${base_libdir}/modules/${PV}
        install -m 0644 ${WORKDIR}/88x2bu.ko ${D}/${base_libdir}/modules/${PV}/88x2bu.ko
    fi
}

FILES:${PN} += "${base_libdir}/modules/${PV}/${@bb.utils.contains('DISTRO_FEATURES', '88x2bu', '88x2bu.ko', '', d)}"
COMPATIBLE_MACHINE = "(ma35d1)"
