SUMMARY = "This is a python nuwriter for ma35d1 tool "

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3590eb8d695bdcea3ba57e74adf8a4ed"

inherit pypi setuptools3 native

SRC_URI = "git://github.com/ronaldoussoren/altgraph.git;protocol=https;branch=master"

SRC_URI[sha256sum] = "ad33358114df7c9416cdb8fa1eaa5852166c505118717021c6a8c7c7abbd03dd"

SRCREV = "e288b5332310deac0ef356294a6bf836cfcd0717"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"



BBCLASSEXTEND = "native nativesdk"
