SUMMARY = "python3-ecdsa "

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=66ffc5e30f76cbb5358fe54b645e5a1d"

inherit pypi setuptools3 native

SRC_URI = "git://github.com/warner/python-ecdsa.git;protocol=https;branch=master"
SRC_URI[sha256sum] = "190348041559e21b22a1d65cee485282ca11a6f81d503fddb84d5017e9ed1e49"

SRCREV = "341e0d8be9fedf66fbc9a95630b4ed2138343380"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"



BBCLASSEXTEND = "native nativesdk"
