SUMMARY = "python3-ecdsa "

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=66ffc5e30f76cbb5358fe54b645e5a1d"

inherit native pypi setuptools3

SRCREV= "341e0d8be9fedf66fbc9a95630b4ed2138343380"

SRC_URI = "git://github.com/warner/python-ecdsa.git;protocol=https;branch=master"
S = "${WORKDIR}/git"
B =  "${WORKDIR}/build"

BBCLASSEXTEND = "native nativesdk"
