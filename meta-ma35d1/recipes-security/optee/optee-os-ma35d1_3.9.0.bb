SUMMARY = "OPTEE TA development kit for ma35d1"
LICENSE = "BSD-2-Clause & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c1f21c4f72f372ef38a5a4aee55ec173"

DEPENDS += "\
    dtc-native \
    python3-pycryptodome-native \
    python3-pycryptodomex-native \
    python3-pyelftools-native \
"

PROVIDES += "virtual/optee-os"

OPTEE_SRCREV ?= "1d2d0fbf50c7ead2bcef2a11f6c676979d17de47"
OPTEE_VERSION = "3.9.0"
PV = "${OPTEE_VERSION}"

# FILESEXTRAPATHS:prepend := "${THISDIR}/optee-os-ma35d1:"
#B = "${WORKDIR}/build"
# Configure build dir for externalsrc class usage through devtool
#EXTERNALSRC_BUILD_pn-${PN} = "${WORKDIR}/build"

OPTEEMACHINE ?= "${MACHINE}"
OPTEEOUTPUTMACHINE ?= "${MACHINE}"

COMPATIBLE_MACHINE = "(ma35d1)"

SRC_URI = "git://github.com/OpenNuvoton/MA35D1_optee_os-v3.9.0.git;protocol=https;branch=master"
SRC_URI:append = "\
    file://0001-allow-setting-sysroot-for-libgcc-lookup.patch \
"

SRCREV = "${OPTEE_SRCREV}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

EXTRA_OEMAKE = "\
    CROSS_COMPILE_core=${HOST_PREFIX} \
    CROSS_COMPILE64=${HOST_PREFIX} \
    CFG_ARM64_core=y ta-targets=ta_arm64 \
    NOWERROR=1 \
    LDFLAGS= \
    LIBGCC_LOCATE_CFLAGS=--sysroot=${STAGING_DIR_HOST} \
    comp-cflagscore='--sysroot=${STAGING_DIR_TARGET}' \
"

inherit deploy python3native

PACKAGE_ARCH = "${MACHINE_ARCH}"
RPROVIDES_${PN} += "virtual/optee-os virtual/systemd-bootconf"

export PLATFORM = "${OPTEE_PLATFORM}"
export PLATFORM_FLAVOR = "${OPTEE_PLATFORM_FLAVOR}"

do_compile(){
    if echo ${TFA_DTB} | grep -q "128"; then
            oe_runmake CFG_TZDRAM_START=0x87800000 CFG_SHMEM_START=0x87F00000 -C ${S} O=${B}
    elif echo ${TFA_DTB} | grep -q "512"; then
            oe_runmake CFG_TZDRAM_START=0x9F800000 CFG_SHMEM_START=0x9FF00000 -C ${S} O=${B}
    elif echo ${TFA_DTB} | grep -q "1g"; then
            oe_runmake CFG_TZDRAM_START=0xAF800000 CFG_SHMEM_START=0xAFF00000 -C ${S} O=${B}
    else
            oe_runmake -C ${S} O=${B}
    fi
}

do_install() {
    install -d ${D}${includedir}/optee/export-user_ta/

    for f in  ${B}/export-ta_arm64/* ; do
       cp -aR  $f  ${D}/usr/include/optee/export-user_ta/
    done
}

OPTEE_BOOTCHAIN = "optee"
OPTEE_HEADER = "tee-header_v2"
OPTEE_PAGEABLE = "tee-pageable_v2"
OPTEE_PAGER = "tee-pager_v2"
OPTEE_SUFFIX = "bin"
# Output the ELF generated
OPTEE_ELF = "tee"
OPTEE_ELF_SUFFIX = "elf"
do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 644 ${B}/core/${OPTEE_HEADER}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_HEADER}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_PAGER}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_PAGER}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_PAGEABLE}.${OPTEE_SUFFIX} ${DEPLOYDIR}/${OPTEE_PAGEABLE}-${OPTEE_BOOTCHAIN}.${OPTEE_SUFFIX}
    install -m 644 ${B}/core/${OPTEE_ELF}.${OPTEE_ELF_SUFFIX} ${DEPLOYDIR}/${OPTEE_ELF}-${OPTEE_BOOTCHAIN}.${OPTEE_ELF_SUFFIX}
}
addtask deploy before do_build after do_compile

INSANE_SKIP:${PN}-dev = "staticdev"
