
DESCRIPTION = "ma35d1 U-Boot suppporting ma35d1 ev boards."
#SECTION = "bootloaders"
require recipes-bsp/u-boot/u-boot.inc

PROVIDES += "u-boot"
DEPENDS += "dtc-native bc-native flex-native bison-native"

unset _PYTHON_SYSCONFIGDATA_NAME

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

UBOOT_SRC ?= "git://github.com/OpenNuvoton/MA35D1_u-boot-v2020.07.git;protocol=https"

SRCBRANCH = "2020.07"
SRC_URI = "${UBOOT_SRC}"
SRCREV = "master"

SRC_URI += " file://uEnv-spinand.txt \
             file://uEnv-spinand-ubi.cfg \
             file://uEnv-nand.txt \
             file://uEnv-nand-ubi.cfg \
             file://uEnv-sdcard.txt \
           "

PV = "${SRCBRANCH}"
S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

LOCALVERSION ?= "-${SRCBRANCH}"


do_compile_append() {
        unset i j
        for config in ${UBOOT_MACHINE}; do
            i=$(expr $i + 1);
            for type in ${UBOOT_CONFIG}; do
                j=$(expr $j + 1);
                if [ $j -eq $i ]
                then
                    if [ -n "${UBOOT_INITIAL_ENV}" ]; then
                        cp ${WORKDIR}/uEnv-${type}.txt ${B}/${config}/u-boot-initial-env-${type}
                        if [ "${TFA_DTB}" = "ma35d1xx8" ]; then
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/mem=256M/mem=248M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        elif  [ "${TFA_DTB}" = "ma35d1xx7" ]; then
                            if [ "${type}" = "sdcard" ]; then
                                sed -i "s/root=\/dev\/mmcblk1p1/root=\/dev\/mmcblk0p1/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                            sed -i "s/mem=256M/mem=128M/1" ${B}/${config}/u-boot-initial-env-${type}
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/mem=128M/mem=120M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        fi

                        ${B}/${config}/tools/mkenvimage -s 0x10000 -o ${B}/${config}/u-boot-initial-env.bin-${type} ${B}/${config}/u-boot-initial-env-${type}
                    fi
                fi
            done
            unset  j
        done
        unset  i
}

do_deploy_append() {
    if [ -n "${UBOOT_CONFIG}" ]
    then
        for config in ${UBOOT_MACHINE}; do
            i=$(expr $i + 1);
            for type in ${UBOOT_CONFIG}; do
                j=$(expr $j + 1);
                if [ $j -eq $i ]
                then
                    if [ -n "${UBOOT_INITIAL_ENV}" ]; then
                        ln -sf ${UBOOT_INITIAL_ENV}-${MACHINE}-${type}-${PV}-${PR} u-boot-env-${type}
                        cp ${B}/${config}/u-boot-initial-env.bin-${type} ${DEPLOY_DIR_IMAGE}/u-boot-initial-env.bin-${type}
                    fi
                fi
                if [ "${type}" = "spinand" ]; then
                   cp ${WORKDIR}/uEnv-spinand-ubi.cfg ${DEPLOY_DIR_IMAGE}/u-boot-initial-env-spinand-ubi.cfg
                elif [ "${type}" = "nand" ]; then
                   cp ${WORKDIR}/uEnv-nand-ubi.cfg ${DEPLOY_DIR_IMAGE}/u-boot-initial-env-nand-ubi.cfg
                fi
            done
            unset  j
        done
        unset  i
    fi
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(ma35d1)"
