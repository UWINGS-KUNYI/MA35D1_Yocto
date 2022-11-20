SUMMARY = "PyInstaller bundles a Python application and all its dependencies into a single package."
HOMEPAGE = "http://www.pyinstaller.org/"
AUTHOR = "Hartmut Goebel, Giovanni Bajo, David Vierra, David Cortesi, Martin Zibricky <>"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING.txt;md5=6caf9c6da7a5d7bd095b1f2a7b118b8a"

SRCREV = "09b8a1ebd0a62c4e61de61cd33c739c997249a89"

SRC_URI = "git://github.com/pyinstaller/pyinstaller.git;protocol=https;branch=develop"
SRC_URI[sha256sum] = "865025b6809d777bb0f66d8f8ab50cc97dc3dbe0ff09a1ef1f2fd646432714fc"

S = "${WORKDIR}/git"
B =  "${WORKDIR}/build"

RDEPENDS_${PN} = "python3-setuptools python3-altgraph python3-pyinstaller-hooks-contrib"

DEPENDS += "python3-wheel-native"

inherit native pypi setuptools3

BBCLASSEXTEND = "native nativesdk"

INHIBIT_SYSROOT_STRIP = "1"
INSANK_SKIP_${PN}_append = "already-stripped"
