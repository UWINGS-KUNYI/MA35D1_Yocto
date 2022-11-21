SUMMARY = "Community maintained hooks for PyInstaller"
HOMEPAGE = "https://github.com/pyinstaller/pyinstaller-hooks-contrib"
AUTHOR = " <>"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=822bee463f4e00ac4478593130e95ccb"

inherit pypi setuptools3 native

SRC_URI = "git://github.com/pyinstaller/pyinstaller-hooks-contrib.git;protocol=https;branch=master"
SRCREV = "dfc2087824547461c40b54ff8faf229036737b3b"
SRC_URI[sha256sum] = "e06d0881e599d94dc39c6ed1917f0ad9b1858a2478b9892faac18bd48bcdc2de"

S = "${WORKDIR}/git"
B =  "${WORKDIR}/build"

RDEPENDS_${PN} = ""



BBCLASSEXTEND = "native nativesdk"
