SUMMARY = "ARM Trusted Firmware-A for ma35d1"
SECTION = "bootloaders"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://license.rst;md5=1dd070c98a281d18d9eefd938729b031"

inherit deploy

DEPENDS:append = " dtc-native openssl-native"
PROVIDES:append = " virtual/trusted-firmware-a"

PV = "${TF_VERSION}.r1"

SRC_URI = "git://github.com/OpenNuvoton/MA35D1_arm-trusted-firmware-v2.3.git;branch=master;protocol=https"
SRC_URI:append = " file://ssl.patch"

TFA_SRCREV ?= "c6e52429629c2beefd875907190e4b4b756262d4"
SRCREV = "${TFA_SRCREV}"

TFA_SPD:ma35d1 = "opteed"

TF_VERSION = "2.3"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

# Let the Makefile handle setting up the CFLAGS and LDFLAGS as it is
# a standalone application
CFLAGS[unexport] = "1"
LDFLAGS[unexport] = "1"
AS[unexport] = "1"
LD[unexport] = "1"

# No configure
do_configure[noexec] = "1"

# Baremetal, just need a compiler
DEPENDS:remove = "virtual/${TARGET_PREFIX}compilerlibs virtual/libc"

# CC and LD introduce arguments which conflict with those otherwise provided by
# this recipe. The heads of these variables excluding those arguments
# are therefore used instead.
def remove_options_tail (in_string):
    from itertools import takewhile
    return ' '.join(takewhile(lambda x: not x.startswith('-'), in_string.split(' ')))

# Verbose builds, no -Werror
EXTRA_OEMAKE += "V=1 E=0"

# Add platform parameter
EXTRA_OEMAKE += "BUILD_BASE=${B}"

# Use the correct native compiler
EXTRA_OEMAKE += "HOSTCC='${BUILD_CC}'"

# Handle TFA_SPD parameter
EXTRA_OEMAKE += "${@'SPD=${TFA_SPD}' if d.getVar('TFA_SPD') else ''}"

# Runtime variables
EXTRA_OEMAKE += "RUNTIME_SYSROOT=${STAGING_DIR_HOST}"

# reduce log level for kirkstone/bl31 failed
EXTRA_OEMAKE += "PLAT_LOG_LEVEL_ASSERT=40"

BUILD_DIR = "${B}/${TFA_PLATFORM}"
BUILD_DIR .= "${@'/${TFA_BOARD}' if d.getVar('TFA_BOARD') else ''}"
BUILD_DIR .= "/${@'debug' if d.getVar("TFA_DEBUG") == '1' else 'release'}"

EXTRA_OEMAKE += "OPENSSL_DIR=${STAGING_DIR_NATIVE}${prefix_native}"

# Configure ma35d1 make settings
PLATFORM = "${TFA_PLATFORM}"
export CROSS_COMPILE="${TARGET_PREFIX}"
export ARCH="aach64"

do_compile() {
    # This is still needed to have the native tools executing properly by
    # setting the RPATH
    sed -i '/^LDLIBS/ s,$, \$\{BUILD_LDFLAGS},' ${S}/tools/fiptool/Makefile
    sed -i '/^INCLUDE_PATHS/ s,$, \$\{BUILD_CFLAGS},' ${S}/tools/fiptool/Makefile
    sed -i '/^LIB/ s,$, \$\{BUILD_LDFLAGS},' ${S}/tools/cert_create/Makefile

    TFA_OPT="NEED_BL31=yes NEED_BL33=yes"
    if [ "${TFA_LOAD_M4}" = "yes" ]; then
        TFA_OPT="${TFA_OPT} NEED_SCP_BL2=yes"
    fi
    if ${@bb.utils.contains('MACHINE_FEATURES', 'optee', 'true', 'false', d)}; then
        TFA_OPT="${TFA_OPT} NEED_BL32=yes"
        if echo ${TFA_DTB} | grep -q "256"; then
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} realclean
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} all
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} fiptool
        elif echo ${TFA_DTB} | grep -q "128"; then
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x07800000 \
                MA35D1_DDR_MAX_SIZE=0x8000000 \
                MA35D1_DRAM_S_BASE=0x87800000 \
                MA35D1_BL32_BASE=0x87800000 -C ${S} realclean
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x07800000 \
                MA35D1_DDR_MAX_SIZE=0x8000000 \
                MA35D1_DRAM_S_BASE=0x87800000 \
                MA35D1_BL32_BASE=0x87800000 -C ${S} all
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x07800000 \
                MA35D1_DDR_MAX_SIZE=0x8000000 \
                MA35D1_DRAM_S_BASE=0x87800000 \
                MA35D1_BL32_BASE=0x87800000 -C ${S} fiptool
        elif echo ${TFA_DTB} | grep -q "512"; then
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x1F800000 \
                MA35D1_DDR_MAX_SIZE=0x20000000 \
                MA35D1_DRAM_S_BASE=0x9F800000 \
                MA35D1_BL32_BASE=0x9F800000 -C ${S} realclean
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x1F800000 \
                MA35D1_DDR_MAX_SIZE=0x20000000 \
                MA35D1_DRAM_S_BASE=0x9F800000 \
                MA35D1_BL32_BASE=0x9F800000 -C ${S} all
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x1F800000 \
                MA35D1_DDR_MAX_SIZE=0x20000000 \
                MA35D1_DRAM_S_BASE=0x9F800000 \
                MA35D1_BL32_BASE=0x9F800000 -C ${S} fiptool
        elif echo ${TFA_DTB} | grep -q "1g"; then
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x3F800000 \
                MA35D1_DDR_MAX_SIZE=0x40000000 \
                MA35D1_DRAM_S_BASE=0xBF800000 \
                MA35D1_BL32_BASE=0xBF800000 -C ${S} realclean
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x3F800000 \
                MA35D1_DDR_MAX_SIZE=0x40000000 \
                MA35D1_DRAM_S_BASE=0xBF800000 \
                MA35D1_BL32_BASE=0xFB800000 -C ${S} all
            oe_runmake PLAT=${PLATFORM} ${TFA_OPT} \
                MA35D1_DRAM_SIZE=0x3F800000 \
                MA35D1_DDR_MAX_SIZE=0x40000000 \
                MA35D1_DRAM_S_BASE=0xBF800000 \
                MA35D1_BL32_BASE=0xBF800000 -C ${S} fiptool
        fi
    else
       oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} realclean
       oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} all
       oe_runmake PLAT=${PLATFORM} ${TFA_OPT} -C ${S} fiptool
    fi
}

do_deploy() {
    install -Dm 0644 ${B}/${PLATFORM}/release/bl2.bin ${DEPLOYDIR}/${BOOT_TOOLS}/bl2-${PLATFORM}.bin
    install -Dm 0644 ${B}/${PLATFORM}/release/fdts/${TFA_DTB}.dtb ${DEPLOYDIR}/${BOOT_TOOLS}/bl2-${PLATFORM}.dtb
    install -Dm 0644 ${B}/${PLATFORM}/release/bl31.bin ${DEPLOYDIR}/${BOOT_TOOLS}/bl31-${PLATFORM}.bin
    install -Dm 755 ${S}/tools/fiptool/fiptool  ${DEPLOYDIR}/${BOOT_TOOLS}/fiptool
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
COMPATIBLE_MACHINE = "(ma35d1)"

addtask deploy after do_compile
