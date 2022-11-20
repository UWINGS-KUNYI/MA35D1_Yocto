SUMMARY = "This is a python nuwriter for ma35d1 tool "

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3590eb8d695bdcea3ba57e74adf8a4ed"

inherit native pypi setuptools3

SRCREV= "e288b5332310deac0ef356294a6bf836cfcd0717"

SRC_URI = "git://github.com/ronaldoussoren/altgraph.git;protocol=https;branch=master"
S = "${WORKDIR}/git"
B =  "${WORKDIR}/build"

BBCLASSEXTEND = "native nativesdk"
