SUMMARY = "This is a python nuwriter for ma35d1 tool "

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e49f4652534af377a713df3d9dec60cb"

inherit setuptools3 deploy native

DEPENDS = "\
    jq-native \
    libusb1-native \
    python3-altgraph-native \
    python3-crcmod-native \
    python3-ecdsa-native \
    python3-native \
    python3-pycryptodome-native \
    python3-pyinstaller-hooks-contrib-native \
    python3-pyinstaller-native \
    python3-pyusb-native \
    python3-six-native \
    python3-tqdm-native \
"

PV = "0.90.0+git${SRCPV}"

SRC_URI = "git://github.com/OpenNuvoton/MA35D1_NuWriter.git;protocol=https;branch=master"
SRC_URI:append = " \
    file://header-nand.json \
    file://header-sdcard.json \
    file://header-spinand.json \
    file://pack-nand.json \
    file://pack-sdcard.json \
    file://pack-spinand.json \
"

SRCREV = "master"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

do_compile(){
    pyinstaller --clean --win-private-assemblies ${S}/nuwriter.py -D -n nuwriter -y --distpath ${B}
}

do_install(){
    install -d ${D}${bindir}
    install -d ${D}${datadir}/nuwriter
    install -d ${D}${datadir}/nuwriter/ddrimg
    install -m 775 ${B}/nuwriter/nuwriter ${D}${bindir}/
    install -m 664 ${WORKDIR}/header-nand.json  ${D}${datadir}/nuwriter/
    install -m 664 ${WORKDIR}/header-sdcard.json  ${D}${datadir}/nuwriter/
    install -m 664 ${WORKDIR}/header-spinand.json  ${D}${datadir}/nuwriter/
    install -m 664 ${WORKDIR}/pack-nand.json  ${D}${datadir}/nuwriter/
    install -m 664 ${WORKDIR}/pack-spinand.json  ${D}${datadir}/nuwriter/
    install -m 664 ${WORKDIR}/pack-sdcard.json  ${D}${datadir}/nuwriter/

    install -m 664 ${S}/ddrimg/* ${D}${datadir}/nuwriter/ddrimg/
    install -m 664 ${S}/xusb.bin ${D}${datadir}/nuwriter/
}

do_deploy() {
    install -d ${DEPLOYDIR}/${BOOT_TOOLS}/nuwriter
    install -d ${DEPLOYDIR}/${BOOT_TOOLS}/nuwriter/ddrimg
    cp -rf ${B}/nuwriter/* ${DEPLOYDIR}/${BOOT_TOOLS}/nuwriter

    cp ${S}/ddrimg/* ${DEPLOYDIR}/${BOOT_TOOLS}/nuwriter/ddrimg/
    cp ${S}/xusb.bin  ${DEPLOYDIR}/${BOOT_TOOLS}/nuwriter/
    cp ${S}/xusb.bin  ${DEPLOYDIR}/${BOOT_TOOLS}/
}

addtask install after do_compile
addtask deploy after do_compile

INHIBIT_SYSROOT_STRIP = "1"
INSANK_SKIP:${PN}:append = " already-stripped"

BBCLASSEXTEND = "native nativesdk"
