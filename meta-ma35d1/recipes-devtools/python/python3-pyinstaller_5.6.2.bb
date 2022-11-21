SUMMARY = "PyInstaller bundles a Python application and all its dependencies into a single package."
HOMEPAGE = "http://www.pyinstaller.org/"
AUTHOR = "Hartmut Goebel, Giovanni Bajo, David Vierra, David Cortesi, Martin Zibricky <>"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://COPYING.txt;md5=371fe7fdee041250f12b3a4658a14278"

inherit pypi setuptools3 native

SRCREV = "09b8a1ebd0a62c4e61de61cd33c739c997249a89"

SRC_URI = "git://github.com/pyinstaller/pyinstaller.git;protocol=https;branch=develop"
SRC_URI[sha256sum] = "865025b6809d777bb0f66d8f8ab50cc97dc3dbe0ff09a1ef1f2fd646432714fc"

S = "${WORKDIR}/git"
B =  "${WORKDIR}/build"

RDEPENDS_${PN} = "python3-setuptools python3-altgraph python3-pyinstaller-hooks-contrib"

DEPENDS += "python3-wheel-native"



BBCLASSEXTEND = "native nativesdk"

INHIBIT_SYSROOT_STRIP = "1"
INSANK_SKIP:${PN}:append = "already-stripped"
