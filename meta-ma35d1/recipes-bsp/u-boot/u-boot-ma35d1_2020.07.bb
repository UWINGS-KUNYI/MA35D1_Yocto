
DESCRIPTION = "ma35d1 U-Boot suppporting ma35d1 ev boards."
#SECTION = "bootloaders"
require recipes-bsp/u-boot/u-boot.inc
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS += "bc-native bison-native dtc-native flex-native u-boot-scr-ma35d1"

unset _PYTHON_SYSCONFIGDATA_NAME

PROVIDES += "u-boot"

PV = "${SRCBRANCH}+git${SRCPV}"
LOCALVERSION ?= "-${SRCBRANCH}"

UBOOT_SRC ?= "git://github.com/OpenNuvoton/MA35D1_u-boot-v2020.07.git;branch=master;protocol=https"
SRCBRANCH = "2020.07"
SRC_URI = "${UBOOT_SRC}"
SRC_URI:append = " \
    file://uEnv-spinand-ubi.cfg \
    file://uEnv-nand-ubi.cfg \
"

UBOOT_SRCREV ?= "00218d51deef4c3322b5fabc974b1afd5067647f"
SRCREV = "${UBOOT_SRCREV}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

do_compile:append() {
        unset i j
        for config in ${UBOOT_MACHINE}; do
            i=$(expr $i + 1);
            for type in ${UBOOT_CONFIG}; do
                j=$(expr $j + 1);
                if [ $j -eq $i ]
                then
                    if [ -n "${UBOOT_INITIAL_ENV}" ]; then
                        if [ "$(grep "CONFIG_SYS_REDUNDAND_ENVIRONMENT=y" ${B}/${config}/.config)" ]; then
                            ENVOPT="-r -s 0x10000 -o"
                        else
                            ENVOPT="-s 0x10000 -o"
                        fi
                        if echo ${TFA_DTB} | grep -q "512"; then
                            sed -i "s/kernelmem=256M/kernelmem=512M/1" ${B}/${config}/u-boot-initial-env-${type}
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/kernelmem=512M/kernelmem=504M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        elif echo ${TFA_DTB} | grep -q "256"; then
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/kernelmem=256M/kernelmem=248M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        elif echo ${TFA_DTB} | grep -q "128"; then
                            sed -i "s/kernelmem=256M/kernelmem=128M/1" ${B}/${config}/u-boot-initial-env-${type}
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/kernelmem=128M/kernelmem=120M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        elif echo ${TFA_DTB} | grep -q "1g"; then
                            sed -i "s/kernelmem=256M/kernelmem=1024M/1" ${B}/${config}/u-boot-initial-env-${type}
                            if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
                                sed -i "s/kernelmem=1024M/kernelmem=1016M/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        fi

                        if [ "x${type}" = "xsdcard" ]; then
                            if [ "x${config}" = "xma35d1_sdcard0_defconfig" ]; then
                                sed -i "s/mmc_block=mmcblk1p1/mmc_block=mmcblk0p1/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                            if echo "${MACHINE}" | grep -q "iot"; then
                                sed -i "s/mmc_block=mmcblk1p1/mmc_block=mmcblk0p1/1" ${B}/${config}/u-boot-initial-env-${type}
                            fi
                        elif [ "x${type}" = "xspinand" ]; then
                            sed -i "s/boot_targets=/boot_targets=mtd0 /1" ${B}/${config}/u-boot-initial-env-${type}
                        elif [ "x${type}" = "xnand" ]; then
                            sed -i "s/boot_targets=/boot_targets=nand0 /1" ${B}/${config}/u-boot-initial-env-${type}
                        fi

                        ${B}/${config}/tools/mkenvimage ${ENVOPT} ${B}/${config}/u-boot-initial-env.bin-${type} ${B}/${config}/u-boot-initial-env-${type}
                    fi
                fi
            done
            unset  j
        done
        unset  i
}

do_deploy:append() {
    unset i j
    if [ -n "${UBOOT_CONFIG}" ]; then
        for config in ${UBOOT_MACHINE}; do
            i=$(expr $i + 1);
            for type in ${UBOOT_CONFIG}; do
                j=$(expr $j + 1);
                if [ $j -eq $i ]
                then
                    if [ -n "${UBOOT_INITIAL_ENV}" ]; then
                        ln -sf ${UBOOT_INITIAL_ENV}-${MACHINE}-${type}-${PV}-${PR} u-boot-env-${type}
                        cp ${B}/${config}/u-boot-initial-env.bin-${type} ${DEPLOYDIR}
                    fi
                fi

                if [ "x${type}" = "xspinand" ]; then
                   cp ${WORKDIR}/uEnv-spinand-ubi.cfg ${DEPLOYDIR}/u-boot-initial-env-spinand-ubi.cfg
                elif [ "x${type}" = "xnand" ]; then
                   cp ${WORKDIR}/uEnv-nand-ubi.cfg ${DEPLOYDIR}/u-boot-initial-env-nand-ubi.cfg
                fi
            done
            unset  j
        done
        unset  i
    fi
}

do_deploy_setscene:apply() {

    if [ -n "${UBOOT_CONFIG}" ]; then
        for type in ${UBOOT_CONFIG}; do
            if [ -n "${UBOOT_INITIAL_ENV}" ]; then
                cp ${DEPLOYDIR}/u-boot-initial-env.bin-${type} ${DEPLOY_IMAGE_DIR}
            fi
        done
    fi

    if [ -f ${DEPLOYDIR}/u-boot-initial-env-spinand-ubi.cfg ]; then
        cp ${DEPLOYDIR}/u-boot-initial-env-spinand-ubi.cfg ${DEPLOY_IMAGE_DIR}
    fi
    if [ -f ${DEPLOYDIR}/u-boot-initial-env-nand-ubi.cfg ]; then
        cp ${DEPLOYDIR}/u-boot-initial-env-nand-ubi.cfg ${DEPLOY_IMAGE_DIR}
    fi
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(ma35d1)"
