SUMMARY = "Eclipse IDE for Nuvoton"
LICENSE = "EPL-1.0"
LIC_FILES_CHKSUM = "file://uninstall.sh;md5=39df560014ac09d72b216a22f2a68ce1"

inherit native

SRC_URI = "https://www.nuvoton.com/opencms/resource-download.jsp?tp_GUID=SW132022111608014594;downloadfilename=nu-eclipse-v1.02.021.tar.gz;name=nu-eclipse"
SRC_URI[nu-eclipse.md5sum] = "e7d042b0cc07a598d2bcd6963bc81e7d"
SRC_URI[nu-eclipse.sha256sum] = "5ff91759943683943aeb323aff1dfabb857485e519edb3e8b01a40067476abcb"

S = "${WORKDIR}/NuEclipse_V1.02.021_Linux_Setup"

do_install() {
	install -d ${D}/${datadir}/nu-eclipse
	cp -r ${S}/. ${D}${datadir}/nu-eclipse
}

INSANE_SKIP_${PN} = "already-stripped file-rdeps sysroot_strip ldflag"
LLOW_EMPTY_${PN} = "1"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
